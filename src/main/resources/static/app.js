const state = {
    token: sessionStorage.getItem('token') || null,
    me: null,
    view: null
};

const $ = (selector) => document.querySelector(selector);

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (c) => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
}

function roleLabel(role) {
    return ({ ROLE_PATIENT: 'Paciente', ROLE_DOCTOR: 'Médico', ROLE_ADMIN: 'Admin' })[role] || role;
}

function formatDate(value) {
    return value ? String(value).replace('T', ' ').slice(0, 16) : '';
}

function normalizeDateTime(value) {
    return value && value.length === 16 ? value + ':00' : value;
}

function dayLabel(day) {
    return ['', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'][day] || day;
}

let messageTimer = null;
function showMessage(text, isError = false) {
    const el = $('#message');
    el.textContent = text;
    el.className = 'message' + (isError ? ' error' : '');
    clearTimeout(messageTimer);
    messageTimer = setTimeout(() => el.classList.add('hidden'), 4500);
}

async function api(path, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (state.token) headers.Authorization = 'Bearer ' + state.token;

    const res = await fetch(path, { ...options, headers });
    if (res.status === 401) {
        logout();
        throw new Error('Sesión expirada. Iniciá sesión nuevamente.');
    }
    if (res.status === 204) return null;

    const text = await res.text();
    let body = null;
    if (text) {
        try { body = JSON.parse(text); } catch { body = text; }
    }
    if (!res.ok) {
        throw new Error((body && body.message) || ('Error ' + res.status));
    }
    return body;
}

function logout() {
    state.token = null;
    state.me = null;
    sessionStorage.removeItem('token');
    renderShell();
}

async function handleLogin(event) {
    event.preventDefault();
    try {
        const data = await api('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email: $('#email').value, password: $('#password').value })
        });
        state.token = data.token;
        sessionStorage.setItem('token', data.token);
        await loadMe();
    } catch (err) {
        showMessage(err.message, true);
    }
}

async function loadMe() {
    state.me = await api('/api/auth/me');
    renderShell();
}

function renderShell() {
    const loggedIn = Boolean(state.token && state.me);
    $('#login-view').classList.toggle('hidden', loggedIn);
    $('#app-view').classList.toggle('hidden', !loggedIn);
    $('#session').classList.toggle('hidden', !loggedIn);
    if (!loggedIn) return;
    $('#session-user').textContent = `${state.me.firstName} ${state.me.lastName} · ${roleLabel(state.me.role)}`;
    buildNav();
}

const VIEWS = {
    ROLE_PATIENT: [
        { id: 'mis-turnos', label: 'Mis turnos', render: viewMisTurnos }
    ],
    ROLE_DOCTOR: [
        { id: 'agenda', label: 'Mi agenda', render: viewAgenda },
        { id: 'historial', label: 'Historial clínico', render: viewHistorial }
    ],
    ROLE_ADMIN: [
        { id: 'turnos', label: 'Turnos', render: viewTurnosAdmin },
        { id: 'usuarios', label: 'Usuarios', render: viewUsuarios },
        { id: 'especialidades', label: 'Especialidades', render: viewEspecialidades },
        { id: 'medicos', label: 'Médicos', render: viewMedicos },
        { id: 'horarios', label: 'Horarios', render: viewHorarios }
    ]
};

function buildNav() {
    const views = VIEWS[state.me.role] || [];
    const nav = $('#nav');
    nav.innerHTML = views.map(v => `<button type="button" data-view="${v.id}">${v.label}</button>`).join('');
    nav.querySelectorAll('button').forEach(btn => btn.addEventListener('click', () => openView(btn.dataset.view)));
    openView(views.length ? views[0].id : null);
}

function openView(viewId) {
    const views = VIEWS[state.me.role] || [];
    const view = views.find(v => v.id === viewId);
    $('#nav').querySelectorAll('button').forEach(b => b.classList.toggle('active', b.dataset.view === viewId));
    if (!view) { $('#content').innerHTML = ''; return; }
    state.view = viewId;
    view.render().catch(err => showMessage(err.message, true));
}

function statusBadge(status) {
    return `<span class="badge ${status || ''}">${status || '-'}</span>`;
}

function statusButtons(appointment) {
    if (appointment.status === 'CANCELLED' || appointment.status === 'COMPLETED') return '';
    const buttons = [];
    if (appointment.status === 'PENDING') {
        buttons.push(`<button class="btn small" data-status="CONFIRMED" data-appt="${appointment.id}">Confirmar</button>`);
    }
    buttons.push(`<button class="btn small" data-status="COMPLETED" data-appt="${appointment.id}">Completar</button>`);
    buttons.push(`<button class="btn small danger" data-status="CANCELLED" data-appt="${appointment.id}">Cancelar</button>`);
    return buttons.join(' ');
}

function attachStatusHandlers(reload) {
    $('#content').querySelectorAll('[data-status]').forEach(btn => btn.addEventListener('click', async () => {
        try {
            await api(`/api/appointments/${btn.dataset.appt}/status`, {
                method: 'PATCH',
                body: JSON.stringify({ status: btn.dataset.status })
            });
            showMessage('Estado actualizado.');
            await reload();
        } catch (err) {
            showMessage(err.message, true);
        }
    }));
}

async function viewMisTurnos() {
    const [appointments, doctors] = await Promise.all([
        api(`/api/appointments/patient/${state.me.id}`),
        api('/api/doctors')
    ]);

    const options = doctors.map(d =>
        `<option value="${d.id}">${escapeHtml(d.doctorName || ('Médico #' + d.id))}${d.specialtyName ? ' — ' + escapeHtml(d.specialtyName) : ''}</option>`
    ).join('');

    const rows = appointments.length ? appointments.map(a => `
        <tr>
            <td>${a.id}</td>
            <td>${escapeHtml(a.doctorName || ('#' + a.doctorId))}</td>
            <td>${formatDate(a.appointmentDate)}</td>
            <td>${escapeHtml(a.reason || '')}</td>
            <td>${statusBadge(a.status)}</td>
            <td class="actions">${(a.status === 'PENDING' || a.status === 'CONFIRMED')
                ? `<button class="btn small danger" data-cancel="${a.id}">Cancelar</button>` : ''}</td>
        </tr>`).join('') : '<tr><td colspan="6" class="muted">Sin turnos.</td></tr>';

    $('#content').innerHTML = `
        <div class="card">
            <h2>Agendar turno</h2>
            <form id="book-form" class="row">
                <label>Médico<select id="book-doctor" required>${options}</select></label>
                <label>Fecha y hora<input type="datetime-local" id="book-date" required></label>
                <label>Motivo<input type="text" id="book-reason" maxlength="255"></label>
                <button class="btn primary" type="submit">Agendar</button>
            </form>
        </div>
        <div class="card">
            <h2>Mis turnos</h2>
            <table>
                <thead><tr><th>#</th><th>Médico</th><th>Fecha</th><th>Motivo</th><th>Estado</th><th></th></tr></thead>
                <tbody>${rows}</tbody>
            </table>
        </div>`;

    $('#book-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        try {
            await api('/api/appointments', {
                method: 'POST',
                body: JSON.stringify({
                    patientId: state.me.id,
                    doctorId: Number($('#book-doctor').value),
                    appointmentDate: normalizeDateTime($('#book-date').value),
                    reason: $('#book-reason').value || null
                })
            });
            showMessage('Turno agendado.');
            await viewMisTurnos();
        } catch (err) {
            showMessage(err.message, true);
        }
    });

    $('#content').querySelectorAll('[data-cancel]').forEach(btn => btn.addEventListener('click', async () => {
        try {
            await api(`/api/appointments/${btn.dataset.cancel}/cancel`, { method: 'PATCH' });
            showMessage('Turno cancelado.');
            await viewMisTurnos();
        } catch (err) {
            showMessage(err.message, true);
        }
    }));
}

async function viewAgenda() {
    if (!state.me.doctorId) {
        $('#content').innerHTML = '<div class="card muted">Tu usuario no tiene perfil de médico asociado.</div>';
        return;
    }
    const appointments = await api(`/api/appointments/doctor/${state.me.doctorId}`);
    const rows = appointments.length ? appointments.map(a => `
        <tr>
            <td>${a.id}</td>
            <td>${escapeHtml(a.patientName || ('#' + a.patientId))}</td>
            <td>${formatDate(a.appointmentDate)}</td>
            <td>${escapeHtml(a.reason || '')}</td>
            <td>${statusBadge(a.status)}</td>
            <td class="actions">${statusButtons(a)}</td>
        </tr>`).join('') : '<tr><td colspan="6" class="muted">Sin turnos.</td></tr>';

    $('#content').innerHTML = `
        <div class="card">
            <h2>Mi agenda</h2>
            <table>
                <thead><tr><th>#</th><th>Paciente</th><th>Fecha</th><th>Motivo</th><th>Estado</th><th>Acciones</th></tr></thead>
                <tbody>${rows}</tbody>
            </table>
        </div>`;

    attachStatusHandlers(viewAgenda);
}

async function viewTurnosAdmin() {
    const appointments = await api('/api/appointments');
    const rows = appointments.length ? appointments.map(a => `
        <tr>
            <td>${a.id}</td>
            <td>${escapeHtml(a.patientName || ('#' + a.patientId))}</td>
            <td>${escapeHtml(a.doctorName || ('#' + a.doctorId))}</td>
            <td>${formatDate(a.appointmentDate)}</td>
            <td>${statusBadge(a.status)}</td>
            <td class="actions">${statusButtons(a)}</td>
        </tr>`).join('') : '<tr><td colspan="6" class="muted">Sin turnos.</td></tr>';

    $('#content').innerHTML = `
        <div class="card">
            <h2>Todos los turnos</h2>
            <table>
                <thead><tr><th>#</th><th>Paciente</th><th>Médico</th><th>Fecha</th><th>Estado</th><th>Acciones</th></tr></thead>
                <tbody>${rows}</tbody>
            </table>
        </div>`;

    attachStatusHandlers(viewTurnosAdmin);
}

async function viewUsuarios() {
    const users = await api('/api/users');
    const rows = users.map(u => `
        <tr>
            <td>${u.id}</td>
            <td>${escapeHtml(u.firstName + ' ' + u.lastName)}</td>
            <td>${escapeHtml(u.email)}</td>
            <td>${roleLabel(u.role)}</td>
            <td>${u.isActive ? 'Sí' : 'No'}</td>
            <td class="actions"><button class="btn small danger" data-del-user="${u.id}">Eliminar</button></td>
        </tr>`).join('');

    $('#content').innerHTML = `
        <div class="card">
            <h2>Crear usuario</h2>
            <form id="user-form" class="row">
                <label>Nombre<input id="u-first" required maxlength="100"></label>
                <label>Apellido<input id="u-last" required maxlength="100"></label>
                <label>Email<input type="email" id="u-email" required maxlength="150"></label>
                <label>Contraseña<input type="password" id="u-pass" minlength="6" required></label>
                <label>Rol<select id="u-role">
                    <option value="ROLE_PATIENT">Paciente</option>
                    <option value="ROLE_DOCTOR">Médico</option>
                    <option value="ROLE_ADMIN">Admin</option>
                </select></label>
                <button class="btn primary" type="submit">Crear</button>
            </form>
        </div>
        <div class="card">
            <h2>Usuarios</h2>
            <table>
                <thead><tr><th>#</th><th>Nombre</th><th>Email</th><th>Rol</th><th>Activo</th><th></th></tr></thead>
                <tbody>${rows}</tbody>
            </table>
        </div>`;

    $('#user-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        try {
            await api('/api/users', {
                method: 'POST',
                body: JSON.stringify({
                    firstName: $('#u-first').value,
                    lastName: $('#u-last').value,
                    email: $('#u-email').value,
                    passwordHash: $('#u-pass').value,
                    role: $('#u-role').value,
                    isActive: true
                })
            });
            showMessage('Usuario creado.');
            await viewUsuarios();
        } catch (err) {
            showMessage(err.message, true);
        }
    });

    $('#content').querySelectorAll('[data-del-user]').forEach(btn => btn.addEventListener('click', async () => {
        if (!confirm('¿Eliminar usuario #' + btn.dataset.delUser + '?')) return;
        try {
            await api(`/api/users/${btn.dataset.delUser}`, { method: 'DELETE' });
            showMessage('Usuario eliminado.');
            await viewUsuarios();
        } catch (err) {
            showMessage(err.message, true);
        }
    }));
}

async function viewEspecialidades() {
    const specialties = await api('/api/specialties');
    const rows = specialties.length ? specialties.map(s => `
        <tr>
            <td>${s.id}</td>
            <td>${escapeHtml(s.name)}</td>
            <td>${escapeHtml(s.description || '')}</td>
            <td class="actions"><button class="btn small danger" data-del-spec="${s.id}">Eliminar</button></td>
        </tr>`).join('') : '<tr><td colspan="4" class="muted">Sin especialidades.</td></tr>';

    $('#content').innerHTML = `
        <div class="card">
            <h2>Crear especialidad</h2>
            <form id="spec-form" class="row">
                <label>Nombre<input id="s-name" required maxlength="100"></label>
                <label>Descripción<input id="s-desc" maxlength="1000"></label>
                <button class="btn primary" type="submit">Crear</button>
            </form>
        </div>
        <div class="card">
            <h2>Especialidades</h2>
            <table>
                <thead><tr><th>#</th><th>Nombre</th><th>Descripción</th><th></th></tr></thead>
                <tbody>${rows}</tbody>
            </table>
        </div>`;

    $('#spec-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        try {
            await api('/api/specialties', {
                method: 'POST',
                body: JSON.stringify({ name: $('#s-name').value, description: $('#s-desc').value || null })
            });
            showMessage('Especialidad creada.');
            await viewEspecialidades();
        } catch (err) {
            showMessage(err.message, true);
        }
    });

    $('#content').querySelectorAll('[data-del-spec]').forEach(btn => btn.addEventListener('click', async () => {
        if (!confirm('¿Eliminar especialidad #' + btn.dataset.delSpec + '?')) return;
        try {
            await api(`/api/specialties/${btn.dataset.delSpec}`, { method: 'DELETE' });
            showMessage('Especialidad eliminada.');
            await viewEspecialidades();
        } catch (err) {
            showMessage(err.message, true);
        }
    }));
}

async function viewMedicos() {
    const [doctors, users, specialties] = await Promise.all([
        api('/api/doctors'), api('/api/users'), api('/api/specialties')
    ]);

    const doctorUsers = users.filter(u => u.role === 'ROLE_DOCTOR');
    const userOptions = doctorUsers.map(u =>
        `<option value="${u.id}">${escapeHtml(u.firstName + ' ' + u.lastName)} (#${u.id})</option>`).join('');
    const specOptions = specialties.map(s => `<option value="${s.id}">${escapeHtml(s.name)}</option>`).join('');

    const rows = doctors.length ? doctors.map(d => `
        <tr>
            <td>${d.id}</td>
            <td>${escapeHtml(d.doctorName || ('Usuario #' + d.userId))}</td>
            <td>${escapeHtml(d.licenseNumber || '')}</td>
            <td>${escapeHtml(d.specialtyName || '')}</td>
            <td>${d.hourlyRate ?? ''}</td>
        </tr>`).join('') : '<tr><td colspan="5" class="muted">Sin médicos.</td></tr>';

    $('#content').innerHTML = `
        <div class="card">
            <h2>Crear perfil de médico</h2>
            <p class="muted">Primero creá el usuario con rol Médico en la pestaña "Usuarios".</p>
            <form id="doctor-form" class="row">
                <label>Usuario médico<select id="d-user" required>${userOptions}</select></label>
                <label>Matrícula<input id="d-license" required maxlength="50"></label>
                <label>Especialidad<select id="d-spec" required>${specOptions}</select></label>
                <label>Tarifa por hora<input type="number" id="d-rate" min="0" step="0.01"></label>
                <button class="btn primary" type="submit">Crear</button>
            </form>
        </div>
        <div class="card">
            <h2>Médicos</h2>
            <table>
                <thead><tr><th>#</th><th>Nombre</th><th>Matrícula</th><th>Especialidad</th><th>Tarifa</th></tr></thead>
                <tbody>${rows}</tbody>
            </table>
        </div>`;

    $('#doctor-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        const rate = $('#d-rate').value;
        try {
            await api('/api/doctors', {
                method: 'POST',
                body: JSON.stringify({
                    userId: Number($('#d-user').value),
                    licenseNumber: $('#d-license').value,
                    specialtyId: Number($('#d-spec').value),
                    hourlyRate: rate ? Number(rate) : null
                })
            });
            showMessage('Médico creado.');
            await viewMedicos();
        } catch (err) {
            showMessage(err.message, true);
        }
    });
}

async function viewHorarios() {
    const doctors = await api('/api/doctors');
    const options = doctors.map(d => `<option value="${d.id}">${escapeHtml(d.doctorName || ('Médico #' + d.id))}</option>`).join('');
    const dayOptions = [1, 2, 3, 4, 5, 6, 7].map(d => `<option value="${d}">${dayLabel(d)}</option>`).join('');

    $('#content').innerHTML = `
        <div class="card">
            <h2>Agregar horario</h2>
            <form id="schedule-form" class="row">
                <label>Médico<select id="sc-doctor" required>${options}</select></label>
                <label>Día<select id="sc-day" required>${dayOptions}</select></label>
                <label>Desde<input type="time" id="sc-start" required></label>
                <label>Hasta<input type="time" id="sc-end" required></label>
                <button class="btn primary" type="submit">Agregar</button>
            </form>
        </div>
        <div class="card">
            <h2>Horarios del médico</h2>
            <label>Médico<select id="sc-view">${options}</select></label>
            <div id="schedule-list"></div>
        </div>`;

    const loadSchedules = async () => {
        const doctorId = $('#sc-view').value;
        if (!doctorId) { $('#schedule-list').innerHTML = ''; return; }
        const schedules = await api(`/api/doctors/${doctorId}/schedules`);
        $('#schedule-list').innerHTML = schedules.length ? `
            <table>
                <thead><tr><th>#</th><th>Día</th><th>Desde</th><th>Hasta</th></tr></thead>
                <tbody>${schedules.map(s => `
                    <tr><td>${s.id}</td><td>${dayLabel(s.dayOfWeek)}</td><td>${s.startTime || ''}</td><td>${s.endTime || ''}</td></tr>
                `).join('')}</tbody>
            </table>` : '<p class="muted">Sin horarios.</p>';
    };

    $('#sc-view').addEventListener('change', () => loadSchedules().catch(err => showMessage(err.message, true)));

    $('#schedule-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        try {
            await api('/api/doctors/schedules', {
                method: 'POST',
                body: JSON.stringify({
                    doctorId: Number($('#sc-doctor').value),
                    dayOfWeek: Number($('#sc-day').value),
                    startTime: $('#sc-start').value + ':00',
                    endTime: $('#sc-end').value + ':00'
                })
            });
            showMessage('Horario agregado.');
            await loadSchedules();
        } catch (err) {
            showMessage(err.message, true);
        }
    });

    await loadSchedules();
}

async function viewHistorial() {
    const records = await api('/api/medical-records');
    const rows = records.length ? records.map(r => `
        <tr>
            <td>${r.id}</td>
            <td>${r.appointmentId}</td>
            <td>${escapeHtml(r.diagnosis)}</td>
            <td>${escapeHtml(r.treatment || '')}</td>
            <td>${escapeHtml(r.notes || '')}</td>
        </tr>`).join('') : '<tr><td colspan="5" class="muted">Sin registros.</td></tr>';

    $('#content').innerHTML = `
        <div class="card">
            <h2>Cargar registro clínico</h2>
            <form id="record-form" class="row">
                <label>ID de turno<input type="number" id="r-appt" required></label>
                <label>Diagnóstico<input id="r-diag" required></label>
                <label>Tratamiento<input id="r-treat"></label>
                <label>Notas<input id="r-notes"></label>
                <button class="btn primary" type="submit">Guardar</button>
            </form>
        </div>
        <div class="card">
            <h2>Registros clínicos</h2>
            <table>
                <thead><tr><th>#</th><th>Turno</th><th>Diagnóstico</th><th>Tratamiento</th><th>Notas</th></tr></thead>
                <tbody>${rows}</tbody>
            </table>
        </div>`;

    $('#record-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        try {
            await api('/api/medical-records', {
                method: 'POST',
                body: JSON.stringify({
                    appointmentId: Number($('#r-appt').value),
                    diagnosis: $('#r-diag').value,
                    treatment: $('#r-treat').value || null,
                    notes: $('#r-notes').value || null
                })
            });
            showMessage('Registro guardado.');
            await viewHistorial();
        } catch (err) {
            showMessage(err.message, true);
        }
    });
}

document.addEventListener('DOMContentLoaded', () => {
    $('#login-form').addEventListener('submit', handleLogin);
    $('#logout').addEventListener('click', logout);
    if (state.token) {
        loadMe().catch(() => logout());
    } else {
        renderShell();
    }
});
