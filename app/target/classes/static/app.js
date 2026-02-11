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
  try {
    return new Date(isoDate).toLocaleString("fr-FR", {
      year: "numeric",
      month: "short",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit"
    });
  } catch (_error) {
    return isoDate;
  }
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

function appointmentCard(appointment) {
  const card = document.createElement("article");
  card.className = "appointment-card";
  card.innerHTML = `
    <p class="appointment-date">${escapeHtml(formatIsoToLocale(appointment.scheduledAt))}</p>
    <h4>${escapeHtml(appointment.patientName)} avec ${escapeHtml(appointment.doctorName)}</h4>
    <p>${escapeHtml(appointment.speciality)} - ${escapeHtml(appointment.reason)}</p>
    <span class="appointment-status">${escapeHtml(appointment.status)}</span>
  `;
  return card;
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
  const appointmentsNode = document.getElementById("appointments");
  appointmentsNode.innerHTML = "";

  if (!appointments.length) {
    appointmentsNode.innerHTML = "<p class=\"empty-state\">Aucun rendez-vous planifie pour le moment.</p>";
    return;
  }

  appointments.forEach((appointment) => appointmentsNode.appendChild(appointmentCard(appointment)));
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

  teleForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    teleMessage.textContent = "";

    const scheduledAtRaw = document.getElementById("scheduledAt").value;
    const payload = {
      patientName: document.getElementById("patientName").value.trim(),
      doctorName: document.getElementById("doctorName").value.trim(),
      speciality: document.getElementById("speciality").value.trim(),
      scheduledAt: new Date(scheduledAtRaw).toISOString(),
      reason: document.getElementById("reason").value.trim()
    };

    try {
      await createAppointment(payload);
      teleMessage.textContent = "Rendez-vous cree avec succes.";
      teleMessage.className = "tele-message success";
      teleForm.reset();
      const appointments = await loadAppointments();
      renderAppointments(appointments);
    } catch (error) {
      teleMessage.textContent = `Echec de creation: ${error.message}`;
      teleMessage.className = "tele-message error";
    }
  });

  await renderAll();
});
