/**
 * simulacion.js
 * Maneja la lógica de la pantalla de simulación, cargando datos dinámicamente
 * desde Supabase (vía window.currentCase).
 */

document.addEventListener('DOMContentLoaded', () => {
    const checkData = setInterval(() => {
        if (window.currentCase) {
            clearInterval(checkData);
            initSimulation(window.currentCase);
        }
    }, 100);
});

function initSimulation(c) {
    console.log("Iniciando simulación SQL para:", c.titulo);

    // --- PESTAÑA CASO ---
    setText('nombre-caso', c.titulo);
    setText('descripcion-caso', c.resumen);
    setText('nombre-paciente', c.nombre_paciente);
    setText('sexo-paciente', c.sexo_paciente);
    setHTML('motivo-consulta', c.motivo_paciente);
    setText('edad-paciente', c.edad_paciente);
    setText('peso-paciente', c.peso_paciente);
    setText('altura-paciente', c.altura_paciente);
    setHTML('enfermedades-paciente', c.antecedentes_paciente);

    // Signos Vitales (Relación 1:1)
    const vitals = c.signos_vitales;
    if (vitals) {
        setText('fr-paciente', vitals.frecuencia_respiratoria);
        setText('fc-paciente', vitals.frecuencia_cardiaca);
        setText('ta-paciente', vitals.presion_arterial);
        setText('temp-paciente', vitals.temperatura);
        setText('sat-paciente', vitals.saturacion);
        initMonitor(vitals);
    }

    // --- PESTAÑA ESTUDIOS (Relación 1:N) ---
    renderEstudios(c.estudios);

    // --- PESTAÑA VIDEOS (PROCEDIMIENTOS) (Relación 1:N) ---
    renderProcedures(c.procedimientos);

    // --- PESTAÑA QUIZ (Relación 1:N) ---
    window.questions = c.preguntas_quiz || [];
    initQuiz();
}

function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.innerText = value || "--";
}

function setHTML(id, value) {
    const el = document.getElementById(id);
    if (el) el.innerHTML = value || "--";
}

function renderEstudios(estudios) {
    const container = document.getElementById('estudios-container');
    if (!container) return;
    container.innerHTML = '';
    if (!estudios || estudios.length === 0) {
        container.innerHTML = '<div class="col-12 text-center text-muted py-4">No hay estudios disponibles.</div>';
        return;
    }
    estudios.forEach(s => {
        const div = document.createElement('div');
        div.className = 'col-md-3 col-sm-6 mb-3';
        div.innerHTML = `
            <div class="card study-card text-center p-3 h-100" onclick="verArchivo('${s.url}', '${s.nombre}')" style="cursor:pointer">
                <i class="bi bi-file-earmark-pdf-fill text-danger" style="font-size: 2.5rem;"></i>
                <div class="mt-2 fw-bold" style="font-size: 0.85rem;">${s.nombre}</div>
            </div>
        `;
        container.appendChild(div);
    });
}

function renderProcedures(procedimientos) {
    const container = document.getElementById('videos-list');
    if (!container) return;
    container.innerHTML = '';
    if (!procedimientos || procedimientos.length === 0) {
        container.innerHTML = '<p class="text-muted">No hay maniobras disponibles para este caso.</p>';
        return;
    }
    procedimientos.forEach(p => {
        const div = document.createElement('div');
        div.className = 'card mb-2 bg-dark border-secondary';
        div.innerHTML = `
            <div class="card-body d-flex align-items-center p-2" onclick="verVideo('${p.url_video}', '${p.nombre}')" style="cursor:pointer">
                <i class="bi bi-play-circle-fill text-danger me-3" style="font-size: 1.5rem;"></i>
                <span class="text-white fw-bold">${p.nombre}</span>
            </div>
        `;
        container.appendChild(div);
    });
}

function initMonitor(vitals) {
    document.getElementById('vi-fc').innerText = vitals.frecuencia_cardiaca;
    document.getElementById('vi-sat').innerText = vitals.saturacion;
    document.getElementById('vi-ta').innerText = vitals.presion_arterial;
    document.getElementById('vi-bis').innerText = vitals.bis || "--";
    document.getElementById('vi-etco2').innerText = vitals.etco2 || "--";
    document.getElementById('vi-fr').innerText = vitals.frecuencia_respiratoria;
    document.getElementById('ecg-bpm-display').innerText = vitals.frecuencia_cardiaca + " BPM";
    document.getElementById('ecg-status-text').innerText = "SISTEMA ACTIVO";
}

let currentQIndex = 0;
function initQuiz() {
    currentQIndex = 0;
    const welcome = document.getElementById('seccionBienvenida');
    const main = document.getElementById('seccionPrincipal');
    if (welcome) welcome.style.display = 'block';
    if (main) main.style.display = 'none';
}

window.iniciarPreguntas = function() {
    if (window.questions.length === 0) {
        alert("No hay preguntas para este caso.");
        return;
    }
    document.getElementById('seccionBienvenida').style.display = 'none';
    document.getElementById('seccionPrincipal').style.display = 'block';
    mostrarPregunta();
};

function mostrarPregunta() {
    const q = window.questions[currentQIndex];
    document.getElementById('preguntaTexto').innerText = q.pregunta;
    const optContainer = document.getElementById('opcionesContainer');
    optContainer.innerHTML = '';
    q.opciones.forEach((opt, idx) => {
        const btn = document.createElement('button');
        btn.className = 'btn btn-outline-secondary w-100 mb-2 text-start';
        btn.innerText = opt;
        btn.onclick = () => verificarRespuesta(idx, q.indice_respuesta_correcta, q.explicacion);
        optContainer.appendChild(btn);
    });
    document.getElementById('explicacionIncorrecta').innerHTML = '';
}

function verificarRespuesta(selected, correct, explanation) {
    const btns = document.querySelectorAll('#opcionesContainer button');
    btns.forEach((b, i) => {
        b.disabled = true;
        if (i === correct) b.className = 'btn btn-success w-100 mb-2 text-start text-white';
        else if (i === selected) b.className = 'btn btn-danger w-100 mb-2 text-start text-white';
    });

    document.getElementById('explicacionIncorrecta').innerHTML = `
        <div class="alert ${selected === correct ? 'alert-success' : 'alert-danger'} mt-2">
            <strong>${selected === correct ? '¡Correcto!' : 'Incorrecto.'}</strong><br>${explanation}
        </div>
    `;
}

window.cambiarPregunta = function() {
    currentQIndex++;
    if (currentQIndex < window.questions.length) {
        mostrarPregunta();
    } else {
        document.getElementById('seccionPrincipal').innerHTML = `
            <div class="text-center py-4">
                <i class="bi bi-check-circle-fill text-success" style="font-size: 3rem;"></i>
                <h4 class="mt-3">¡Has terminado el quiz!</h4>
                <button class="btn btn-primary mt-3" onclick="location.reload()">Reiniciar</button>
            </div>
        `;
    }
};

window.verArchivo = function(url, nombre) {
    document.getElementById('archivoModalLabel').innerText = nombre;
    document.getElementById('archivoIframe').src = url;
    const modal = new bootstrap.Modal(document.getElementById('archivoModal'));
    modal.show();
};

window.verVideo = function(url, nombre) {
    alert("Reproduciendo video: " + nombre + "\nURL: " + url);
};

document.querySelectorAll('.sim-tab').forEach(tab => {
    tab.addEventListener('click', () => {
        document.querySelectorAll('.sim-tab').forEach(t => t.classList.remove('active'));
        document.querySelectorAll('.sim-panel').forEach(p => p.classList.remove('active'));
        tab.classList.add('active');
        const panelId = 'panel-' + tab.getAttribute('data-tab');
        document.getElementById(panelId).classList.add('active');
    });
});
