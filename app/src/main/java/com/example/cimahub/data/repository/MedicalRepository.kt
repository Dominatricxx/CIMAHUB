package com.example.cimahub.data.repository

import com.example.cimahub.data.models.*

object MedicalRepository {
    private val allCases = listOf(
        // ECOE
        ClinicalCase(
            id = 27,
            title = "FIEBRE",
            summary = "Caso clínico de fiebre en carpeta ECOE.",
            folder = "ECOE",
            anamnesis = "wksjksjfk",
            physicalExamination = "No especificada en el registro.",
            vitalSigns = VitalSigns(0, "0/0", 0, 0f, 0),
            simulationUrl = "simulacion.php?id=27"
        ),
        // Sin Carpeta
        ClinicalCase(
            id = 23,
            title = "Apendicitis",
            summary = "Carlos M., 22 años. Dolor en FID y signos apendiculares positivos.",
            folder = "Sin carpeta",
            anamnesis = "Varón de 22 años acude por dolor periumbilical de 18h que migra a FID hace 6h. Náuseas, anorexia y escalofríos.",
            physicalExamination = "Signo de McBurney (+), Blumberg (+), Rovsing (+), Psoas (+). Ruidos peristálticos disminuidos.",
            vitalSigns = VitalSigns(105, "120/75", 20, 38.3f, 98),
            simulationUrl = "simulacion.php?id=23",
            hotspotId = "appendix"
        ),
        ClinicalCase(
            id = 20,
            title = "APNEA",
            summary = "Masculino de 55 años con ronquidos intensos y somnolencia diurna.",
            folder = "Sin carpeta",
            anamnesis = "Cuadro de 3 años con ronquidos intensos, despertares con sensación de ahogo y apneas presenciadas. Cefalea matutina y somnolencia.",
            physicalExamination = "Circunferencia de cuello 46 cm.",
            vitalSigns = VitalSigns(96, "160/100", 24, 36.0f, 95, bis = 98, etco2 = 35),
            simulationUrl = "simulacion.php?id=20",
            hotspotId = "throat",
            patientName = "Juan Pérez",
            patientSex = "Masculino",
            patientAge = 55,
            patientWeight = 95f,
            patientHeight = 175,
            patientMotive = "Ronquidos intensos y somnolencia diurna.",
            patientHistory = "Hipertensión arterial, obesidad grado I.",
            studies = listOf(
                Study("Radiografía de Tórax", "url_rx"),
                Study("Analítica de Sangre", "url_lab")
            ),
            procedures = listOf(
                Procedure("Exploración de Vía Aérea", "url_video")
            ),
            quiz = listOf(
                QuizQuestion(
                    "¿Cuál es el principal factor de riesgo en este paciente?",
                    listOf("Edad", "Obesidad", "Sexo", "Altura"),
                    1,
                    "La obesidad es el factor de riesgo más importante para el SAHOS."
                )
            )
        ),
        ClinicalCase(
            id = 7,
            title = "Derrame pleural",
            summary = "Masculino de 80 años con IR y disnea progresiva.",
            folder = "Sin carpeta",
            anamnesis = "Paciente con DM, Insuficiencia Renal en hemodiálisis e hipertensión. Presenta tos de 5 días, disnea a leves esfuerzos y dolor en hemitórax derecho.",
            physicalExamination = "Afebril, dolor pleurítico.",
            vitalSigns = VitalSigns(85, "130/80", 22, 36.5f, 92),
            simulationUrl = "simulacion.php?id=7",
            hotspotId = "lungs"
        ),
        ClinicalCase(
            id = 9,
            title = "Dislipidemia",
            summary = "Femenino de 52 años con xantomas en manos.",
            folder = "Sin carpeta",
            anamnesis = "Antecedente de HAS e Hipotiroidismo. Presenta cefalea, fatiga, tinnitus y dolor ardoroso en MMII.",
            physicalExamination = "Protuberancias amarillas-naranja (xantomas) en ambas manos.",
            vitalSigns = VitalSigns(75, "140/90", 18, 36.6f, 98),
            simulationUrl = "simulacion.php?id=9"
        ),
        ClinicalCase(
            id = 10,
            title = "Emergencia hipertensiva",
            summary = "Masculino de 58 años con alteración del estado de conciencia.",
            folder = "Sin carpeta",
            anamnesis = "Cefalea de 5 días, pérdidas de conocimiento, vómitos y visión borrosa.",
            physicalExamination = "Glasgow alterado: respuesta verbal inapropiada, apertura ocular al dolor, retirada motriz.",
            vitalSigns = VitalSigns(90, "210/120", 20, 36.5f, 96),
            simulationUrl = "simulacion.php?id=10",
            hotspotId = "brain"
        ),
        ClinicalCase(
            id = 6,
            title = "EPOC 2",
            summary = "Mujer de 72 años con disnea grado III mMRC.",
            folder = "Sin carpeta",
            anamnesis = "Exfumadora (35 paquetes/año). Aumento progresivo de disnea que le impide caminar más de 100m. Tos escasa matutina.",
            physicalExamination = "Sin edemas en MMII ni ortopnea.",
            vitalSigns = VitalSigns(88, "135/85", 22, 36.4f, 88),
            simulationUrl = "simulacion.php?id=6",
            hotspotId = "lungs"
        ),
        ClinicalCase(
            id = 16,
            title = "Escenario 1",
            summary = "Marcela, 58 años. Posible Hipotiroidismo.",
            folder = "Sin carpeta",
            anamnesis = "Refiere agotamiento, caída de pelo, intolerancia al frío, olvidos y tristeza profunda.",
            physicalExamination = "Voz ronca, piel seca y edema facial.",
            vitalSigns = VitalSigns(54, "110/70", 20, 35.9f, 98),
            simulationUrl = "simulacion.php?id=16",
            hotspotId = "thyroid"
        ),
        ClinicalCase(
            id = 4,
            title = "Fiebre a determinar",
            summary = "Paciente pediátrico de 4 años con fiebre persistente.",
            folder = "Sin carpeta",
            anamnesis = "Fiebre de 38.5°C de 4 días de evolución. Inició amoxicilina hace 2 días sin mejoría y agrega diarrea.",
            physicalExamination = "Dermatitis en área anal.",
            vitalSigns = VitalSigns(120, "90/60", 28, 38.5f, 97),
            simulationUrl = "simulacion.php?id=4"
        ),
        ClinicalCase(
            id = 13,
            title = "HG",
            summary = "Femenino de 36 años, embarazo 36 semanas con preeclampsia.",
            folder = "Sin carpeta",
            anamnesis = "Embarazo de 36 semanas, Diabetes Gestacional. Cifras tensionales de 160/90 mmHg.",
            physicalExamination = "Edema facial y en MMII (++), reflejos con clonus, dolor en epigastrio. FCF 176 lpm.",
            vitalSigns = VitalSigns(95, "160/90", 22, 37.0f, 98),
            simulationUrl = "simulacion.php?id=13",
            hotspotId = "uterus"
        ),
        ClinicalCase(
            id = 22,
            title = "Infarto Agudo al Miocardio",
            summary = "Dolor torácico opresivo 9/10 y signo de Levine.",
            folder = "Sin carpeta",
            anamnesis = "Inicio súbito en situación de estrés. Dolor retroesternal irradiado a brazo izquierdo, diaforesis.",
            physicalExamination = "Signo de Levine (+), R3 presente, crepitantes bibasales.",
            vitalSigns = VitalSigns(106, "150/90", 26, 36.0f, 90),
            simulationUrl = "simulacion.php?id=22",
            hotspotId = "heart"
        ),
        ClinicalCase(
            id = 21,
            title = "Insuficiencia Cardiaca",
            summary = "Laura M., 56 años. Disnea progresiva NYHA II-III.",
            folder = "Sin carpeta",
            anamnesis = "Disnea de medianos esfuerzos, ortopnea y tos seca nocturna. Antecedente de HAS y Obesidad I.",
            physicalExamination = "Estertores crepitantes finos bilaterales, R3 (galope ventricular).",
            vitalSigns = VitalSigns(108, "150/95", 22, 36.7f, 93),
            simulationUrl = "simulacion.php?id=21",
            hotspotId = "heart"
        ),
        ClinicalCase(
            id = 18,
            title = "Meniré",
            summary = "Vértigo súbito y plenitud ótica.",
            folder = "Sin carpeta",
            anamnesis = "Vértigo rotatorio súbito, náuseas, plenitud ótica y tinnitus en oído derecho.",
            physicalExamination = "Nistagmo horizontal hacia la izquierda, Romberg (+), marcha con lateropulsión.",
            vitalSigns = VitalSigns(80, "120/80", 18, 36.5f, 99),
            simulationUrl = "simulacion.php?id=18",
            hotspotId = "ear"
        ),
        ClinicalCase(
            id = 8,
            title = "Neumonía",
            summary = "Femenino de 28 años con pérdida de peso y fiebre.",
            folder = "Sin carpeta",
            anamnesis = "Fiebre 38.5°C, pérdida de 8kg en 3 meses. Antecedente de hospitalizaciones por NAC.",
            physicalExamination = " AGO: G3 P3. Palidez cutánea.",
            vitalSigns = VitalSigns(110, "110/70", 24, 38.5f, 94),
            simulationUrl = "simulacion.php?id=8",
            hotspotId = "lungs"
        ),
        ClinicalCase(
            id = 17,
            title = "OMA",
            summary = "Mateo, 4 años. Otalgia y fiebre.",
            folder = "Sin carpeta",
            anamnesis = "Fiebre de 72h (38.8°C), irritabilidad, hipoacusia subjetiva (sube volumen TV). Antecedente de rinitis alérgica.",
            physicalExamination = "Faringe hiperémica, membrana timpánica abombada (inferido).",
            vitalSigns = VitalSigns(115, "100/60", 24, 38.8f, 98),
            simulationUrl = "simulacion.php?id=17",
            hotspotId = "ear"
        )
    )

    fun getCases(): List<ClinicalCase> = allCases
    fun getCaseById(id: Int): ClinicalCase? = allCases.find { it.id == id }
    fun getCasesByFolder(folder: String): List<ClinicalCase> = allCases.filter { it.folder == folder }
    fun getFolders(): List<String> = allCases.map { it.folder }.distinct().sortedBy { if (it == "Sin carpeta") 1 else 0 }
}
