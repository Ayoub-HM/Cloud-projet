const HOME_API = "/api/medisante/home";
const TELECONSULT_API = "/api/medisante/teleconsultations";

const FALLBACK_HOME_DATA = {
  company: "MediSante+",
  label: "Entreprise francaise de telemedecine",
  foundedYear: 2018,
  employees: 120,
  heroImageUrl: "https://images.unsplash.com/photo-1631815588090-d1bcbe9a24b2?auto=format&fit=crop&w=1800&q=80",
  companyStory: "MediSante+ est une entreprise francaise de telemedecine fondee en 2018, employant 120 collaborateurs repartis entre Paris, Lyon et Bordeaux.",
  platformOverview: "La plateforme permet les teleconsultations, le suivi des dossiers et la gestion des prescriptions.",
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
    return isoDate || "-";
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

function isoToInputDateTime(isoDate) {
  const parsedDate = new Date(isoDate);
  if (Number.isNaN(parsedDate.getTime())) {
    return "";
  }
  return toLocalDateTimeInputValue(parsedDate);
}

function normalizeStatus(status) {
  const raw = String(status || "PLANIFIEE").trim().toUpperCase().replace(/\s+/g, "_");
  if (raw === "EN_COURS" || raw === "TERMINEE" || raw === "PLANIFIEE") {
    return raw;
  }
  return "PLANIFIEE";
}

function toStatusClass(status) {
  if (status === "EN_COURS") {
    return "en-cours";
  }
  if (status === "TERMINEE") {
    return "terminee";
  }
  return "planifiee";
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

function td(value) {
  const cell = document.createElement("td");
  cell.textContent = value ?? "-";
  return cell;
}

function appointmentRow(appointment, onEdit, onDelete) {
  const row = document.createElement("tr");
  row.appendChild(td(String(appointment.id ?? "-")));
  row.appendChild(td(appointment.patientName || "-"));
  row.appendChild(td(appointment.doctorName || "-"));
  row.appendChild(td(appointment.speciality || "-"));
  row.appendChild(td(formatIsoToLocale(appointment.scheduledAt)));
  row.appendChild(td(appointment.reason || "-"));

  const status = normalizeStatus(appointment.status);
  const statusCell = document.createElement("td");
  const statusTag = document.createElement("span");
  statusTag.className = `status-pill ${toStatusClass(status)}`;
  statusTag.textContent = status;
  statusCell.appendChild(statusTag);
  row.appendChild(statusCell);

  const actionsCell = document.createElement("td");
  const actionsWrap = document.createElement("div");
  actionsWrap.className = "row-actions";

  const editButton = document.createElement("button");
  editButton.type = "button";
  editButton.className = "action-btn edit";
  editButton.textContent = "Modifier";
  editButton.addEventListener("click", () => onEdit(appointment));

  const deleteButton = document.createElement("button");
  deleteButton.type = "button";
  deleteButton.className = "action-btn delete";
  deleteButton.textContent = "Supprimer";
  deleteButton.addEventListener("click", () => onDelete(appointment));

  actionsWrap.appendChild(editButton);
  actionsWrap.appendChild(deleteButton);
  actionsCell.appendChild(actionsWrap);
  row.appendChild(actionsCell);
  return row;
}

async function parseError(response) {
  const text = await response.text();
  if (!text) {
    return `Erreur ${response.status}`;
  }

  try {
    const body = JSON.parse(text);
    if (body && typeof body.error === "string" && body.error.trim() !== "") {
      return body.error;
    }
  } catch (_error) {
    // keep raw response text fallback
  }

  return text;
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
    showModeBadge("Mode partiel: liste des teleconsultations indisponible.");
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
    throw new Error(await parseError(response));
  }
  return response.json();
}

async function updateAppointment(id, payload) {
  const response = await fetch(`${TELECONSULT_API}/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return response.json();
}

async function deleteAppointment(id) {
  const response = await fetch(`${TELECONSULT_API}/${id}`, { method: "DELETE" });
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
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

function renderAppointments(appointments, onEdit, onDelete) {
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
  appointments.forEach((appointment) => appointmentsBody.appendChild(appointmentRow(appointment, onEdit, onDelete)));
}

function buildPayloadFromForm(form) {
  const scheduledAtDate = new Date(form.scheduledAt.value);
  if (Number.isNaN(scheduledAtDate.getTime())) {
    throw new Error("La date saisie est invalide.");
  }
  return {
    patientName: form.patientName.value.trim(),
    doctorName: form.doctorName.value.trim(),
    speciality: form.speciality.value.trim(),
    scheduledAt: scheduledAtDate.toISOString(),
    reason: form.reason.value.trim(),
    status: normalizeStatus(form.status.value)
  };
}

document.addEventListener("DOMContentLoaded", async () => {
  const teleForm = document.getElementById("teleForm");
  const teleMessage = document.getElementById("teleMessage");
  const submitButton = document.getElementById("submitButton");
  const cancelEditButton = document.getElementById("cancelEditButton");
  const editModeBanner = document.getElementById("editModeBanner");
  const editIdLabel = document.getElementById("editIdLabel");
  const statusInput = document.getElementById("status");
  const scheduledAtInput = document.getElementById("scheduledAt");
  const defaultSubmitText = "Ajouter le rendez-vous";
  const editSubmitText = "Enregistrer la modification";
  let editingId = null;

  function showMessage(message, type) {
    teleMessage.textContent = message;
    teleMessage.className = `tele-message ${type}`;
  }

  function clearMessage() {
    teleMessage.textContent = "";
    teleMessage.className = "tele-message";
  }

  function resetFormToCreateMode() {
    editingId = null;
    teleForm.reset();
    statusInput.value = "PLANIFIEE";
    scheduledAtInput.min = toLocalDateTimeInputValue(new Date());
    submitButton.textContent = defaultSubmitText;
    cancelEditButton.classList.add("hidden");
    editModeBanner.classList.add("hidden");
    editIdLabel.textContent = "";
  }

  function setFormToEditMode(appointment) {
    editingId = appointment.id;
    teleForm.patientName.value = appointment.patientName || "";
    teleForm.doctorName.value = appointment.doctorName || "";
    teleForm.speciality.value = appointment.speciality || "";
    teleForm.reason.value = appointment.reason || "";
    teleForm.scheduledAt.value = isoToInputDateTime(appointment.scheduledAt);
    teleForm.status.value = normalizeStatus(appointment.status);
    scheduledAtInput.min = "";
    submitButton.textContent = editSubmitText;
    cancelEditButton.classList.remove("hidden");
    editModeBanner.classList.remove("hidden");
    editIdLabel.textContent = `#${appointment.id}`;
    clearMessage();
  }

  async function handleDeleteClick(appointment) {
    const confirmed = window.confirm(`Supprimer le rendez-vous #${appointment.id} ?`);
    if (!confirmed) {
      return;
    }
    try {
      await deleteAppointment(appointment.id);
      if (editingId === appointment.id) {
        resetFormToCreateMode();
      }
      showMessage("Rendez-vous supprime avec succes.", "success");
      await refreshAppointments();
    } catch (error) {
      const message = error instanceof Error ? error.message : "Erreur inconnue";
      showMessage(`Echec de suppression: ${message}`, "error");
    }
  }

  async function refreshAppointments() {
    const appointments = await loadAppointments();
    renderAppointments(appointments, setFormToEditMode, handleDeleteClick);
  }

  cancelEditButton.addEventListener("click", () => {
    resetFormToCreateMode();
    clearMessage();
  });

  teleForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearMessage();

    if (!teleForm.checkValidity()) {
      teleForm.reportValidity();
      return;
    }

    let payload;
    try {
      payload = buildPayloadFromForm(teleForm);
    } catch (error) {
      const message = error instanceof Error ? error.message : "Erreur inconnue";
      showMessage(message, "error");
      return;
    }

    submitButton.disabled = true;
    submitButton.textContent = editingId === null ? "Ajout en cours..." : "Mise a jour en cours...";
    try {
      if (editingId === null) {
        await createAppointment(payload);
        showMessage("Rendez-vous ajoute avec succes.", "success");
      } else {
        await updateAppointment(editingId, payload);
        showMessage("Rendez-vous modifie avec succes.", "success");
      }
      resetFormToCreateMode();
      await refreshAppointments();
    } catch (error) {
      const message = error instanceof Error ? error.message : "Erreur inconnue";
      showMessage(`Echec: ${message}`, "error");
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = editingId === null ? defaultSubmitText : editSubmitText;
    }
  });

  clearModeBadge();
  const [homeData] = await Promise.all([loadHomeData()]);
  renderHome(homeData);
  resetFormToCreateMode();
  await refreshAppointments();
});
