const HOME_API = "/api/medisante/home";
const TELECONSULT_API = "/api/medisante/teleconsultations";

const FALLBACK_HOME_DATA = {
  company: "MediSante+",
  label: "Entreprise francaise de telemedecine",
  foundedYear: 2018,
  employees: 120,
  heroImageUrl: "https://images.unsplash.com/photo-1631815588090-d1bcbe9a24b2?auto=format&fit=crop&w=1800&q=80",
  companyStory: "MediSante+ est une entreprise francaise de telemedecine fondee en 2018, employant 120 collaborateurs repartis entre Paris (siege), Lyon et Bordeaux.",
  platformOverview: "L'entreprise propose une plateforme permettant aux patients de consulter des medecins a distance, de gerer leurs dossiers medicaux et de recevoir des prescriptions electroniques.",
  services: [
    {
      title: "Consultations video securisees",
      description: "Mise en relation rapide patient-medecin avec historique de consultation.",
      category: "Teleconsultation",
      imageUrl: "https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=1200&q=80"
    },
    {
      title: "Gestion des dossiers medicaux",
      description: "Centralisation des comptes rendus, analyses et ordonnances.",
      category: "Dossier medical",
      imageUrl: "https://images.unsplash.com/photo-1576091160550-2173dba999ef?auto=format&fit=crop&w=1200&q=80"
    },
    {
      title: "Prescriptions electroniques",
      description: "Emission d'ordonnances numeriques et suivi du renouvellement.",
      category: "Prescription",
      imageUrl: "https://images.unsplash.com/photo-1585435557343-3b092031a831?auto=format&fit=crop&w=1200&q=80"
    }
  ],
  offices: [
    { city: "Paris", role: "Siege social", employees: 65 },
    { city: "Lyon", role: "Pole operations medicales", employees: 32 },
    { city: "Bordeaux", role: "Support patient et qualite", employees: 23 }
  ]
};

function escapeHtml(value) {
  return String(value).replace(/[&<>"'`]/g, (c) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    "\"": "&quot;",
    "'": "&#39;",
    "`": "&#96;"
  }[c]));
}

function formatIsoToLocale(isoDate) {
  const parsedDate = new Date(isoDate);
  if (Number.isNaN(parsedDate.getTime())) {
    return isoDate;
  }
  return parsedDate.toLocaleString("fr-FR", {
    year: "numeric",
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}

function toLocalDateTimeInputValue(date) {
  const pad = (value) => String(value).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function showModeBadge(message) {
  const badge = document.getElementById("modeBadge");
  badge.textContent = message;
  badge.classList.remove("hidden");
}

function clearModeBadge() {
  const badge = document.getElementById("modeBadge");
  badge.textContent = "";
  badge.classList.add("hidden");
}

function serviceCard(service) {
  const article = document.createElement("article");
  article.className = "card service-card";
  article.innerHTML = `
    <img src="${escapeHtml(service.imageUrl)}" alt="${escapeHtml(service.title)}" loading="lazy" />
    <div class="card-content">
      <p class="card-pill">${escapeHtml(service.category)}</p>
      <h3>${escapeHtml(service.title)}</h3>
      <p>${escapeHtml(service.description)}</p>
    </div>
  `;
  return article;
}

function officeCard(office) {
  const article = document.createElement("article");
  article.className = "card office-card";
  article.innerHTML = `
    <h3>${escapeHtml(office.city)}</h3>
    <p>${escapeHtml(office.role)}</p>
    <p class="office-count">${escapeHtml(office.employees)} collaborateurs</p>
  `;
  return article;
}

function toStatusClass(status) {
  const normalized = String(status || "").toLowerCase();
  if (normalized.includes("cours")) {
    return "en-cours";
  }
  if (normalized.includes("term")) {
    return "terminee";
  }
  return "planifiee";
}

function td(value) {
  const cell = document.createElement("td");
  cell.textContent = value ?? "-";
  return cell;
}

function appointmentRow(appointment) {
  const row = document.createElement("tr");
  row.appendChild(td(String(appointment.id ?? "-")));
  row.appendChild(td(appointment.patientName || "-"));
  row.appendChild(td(appointment.doctorName || "-"));
  row.appendChild(td(appointment.speciality || "-"));
  row.appendChild(td(formatIsoToLocale(appointment.scheduledAt)));
  row.appendChild(td(appointment.reason || "-"));

  const statusCell = document.createElement("td");
  const status = appointment.status || "PLANIFIEE";
  const statusTag = document.createElement("span");
  statusTag.className = `status-pill ${toStatusClass(status)}`;
  statusTag.textContent = status;
  statusCell.appendChild(statusTag);
  row.appendChild(statusCell);
  return row;
}

async function getJson(url) {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Erreur ${response.status} sur ${url}`);
  }
  return response.json();
}

async function loadHomeData() {
  try {
    return await getJson(HOME_API);
  } catch (_error) {
    showModeBadge("Mode demo actif: API home indisponible, donnees de secours affichees.");
    return FALLBACK_HOME_DATA;
  }
}

async function loadAppointments() {
  try {
    return await getJson(TELECONSULT_API);
  } catch (_error) {
    showModeBadge("Mode partiel: la liste des teleconsultations n'est pas accessible.");
    return [];
  }
}

async function createAppointment(payload) {
  const response = await fetch(TELECONSULT_API, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Erreur ${response.status}`);
  }

  return response.json();
}

function renderHome(data) {
  document.getElementById("companyTitle").textContent = data.company;
  document.getElementById("companyStory").textContent = data.companyStory;
  document.getElementById("companyOverview").textContent = data.platformOverview;
  document.getElementById("foundedYear").textContent = data.foundedYear;
  document.getElementById("employees").textContent = data.employees;
  document.getElementById("heroImage").src = data.heroImageUrl;

  const servicesNode = document.getElementById("services");
  servicesNode.innerHTML = "";
  data.services.forEach((service) => servicesNode.appendChild(serviceCard(service)));

  const officesNode = document.getElementById("offices");
  officesNode.innerHTML = "";
  data.offices.forEach((office) => officesNode.appendChild(officeCard(office)));
}

function renderAppointments(appointments) {
  const appointmentsBody = document.getElementById("appointmentsBody");
  const appointmentsEmpty = document.getElementById("appointmentsEmpty");
  const appointmentCount = document.getElementById("appointmentCount");
  appointmentsBody.innerHTML = "";
  appointmentCount.textContent = String(appointments.length);

  if (!appointments.length) {
    appointmentsEmpty.classList.remove("hidden");
    return;
  }

  appointmentsEmpty.classList.add("hidden");
  appointments.forEach((appointment) => appointmentsBody.appendChild(appointmentRow(appointment)));
}

async function renderAll() {
  clearModeBadge();
  const [homeData, appointments] = await Promise.all([loadHomeData(), loadAppointments()]);
  renderHome(homeData);
  renderAppointments(appointments);
}

document.addEventListener("DOMContentLoaded", async () => {
  const teleMessage = document.getElementById("teleMessage");
  const teleForm = document.getElementById("teleForm");
  const scheduledAtInput = document.getElementById("scheduledAt");
  const submitButton = document.getElementById("submitButton");
  const defaultSubmitLabel = submitButton.textContent;
  scheduledAtInput.min = toLocalDateTimeInputValue(new Date());

  teleForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    teleMessage.textContent = "";
    teleMessage.className = "tele-message";

    if (!teleForm.checkValidity()) {
      teleForm.reportValidity();
      return;
    }

    const scheduledAtRaw = document.getElementById("scheduledAt").value;
    const scheduledAtDate = new Date(scheduledAtRaw);
    if (Number.isNaN(scheduledAtDate.getTime())) {
      teleMessage.textContent = "La date saisie est invalide.";
      teleMessage.className = "tele-message error";
      return;
    }

    const payload = {
      patientName: document.getElementById("patientName").value.trim(),
      doctorName: document.getElementById("doctorName").value.trim(),
      speciality: document.getElementById("speciality").value.trim(),
      scheduledAt: scheduledAtDate.toISOString(),
      reason: document.getElementById("reason").value.trim()
    };

    submitButton.disabled = true;
    submitButton.textContent = "Enregistrement...";
    try {
      await createAppointment(payload);
      teleMessage.textContent = "Rendez-vous cree avec succes.";
      teleMessage.className = "tele-message success";
      teleForm.reset();
      scheduledAtInput.min = toLocalDateTimeInputValue(new Date());
      const appointments = await loadAppointments();
      renderAppointments(appointments);
    } catch (error) {
      const message = error instanceof Error ? error.message : "Erreur inconnue";
      teleMessage.textContent = `Echec de creation: ${message}`;
      teleMessage.className = "tele-message error";
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = defaultSubmitLabel;
    }
  });

  await renderAll();
});
