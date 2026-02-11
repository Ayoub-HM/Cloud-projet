const apiRoot = "/api/medisante/home";

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

function serviceCard(service) {
  const article = document.createElement("article");
  article.className = "card";
  article.innerHTML = `
    <p class="card-pill">${escapeHtml(service.category)}</p>
    <h3>${escapeHtml(service.title)}</h3>
    <p>${escapeHtml(service.description)}</p>
  `;
  return article;
}

function officeCard(office) {
  const article = document.createElement("article");
  article.className = "card";
  article.innerHTML = `
    <h3>${escapeHtml(office.city)}</h3>
    <p>${escapeHtml(office.role)}</p>
    <p class="office-count">${escapeHtml(office.employees)} collaborateurs</p>
  `;
  return article;
}

async function loadHomeData() {
  const response = await fetch(apiRoot);
  if (!response.ok) {
    throw new Error(`Impossible de charger les donnees (${response.status})`);
  }
  return response.json();
}

async function render() {
  const data = await loadHomeData();

  document.getElementById("companyTitle").textContent = data.company;
  document.getElementById("companyStory").textContent = data.companyStory;
  document.getElementById("companyOverview").textContent = data.platformOverview;
  document.getElementById("foundedYear").textContent = data.foundedYear;
  document.getElementById("employees").textContent = data.employees;

  const servicesNode = document.getElementById("services");
  servicesNode.innerHTML = "";
  data.services.forEach((service) => servicesNode.appendChild(serviceCard(service)));

  const officesNode = document.getElementById("offices");
  officesNode.innerHTML = "";
  data.offices.forEach((office) => officesNode.appendChild(officeCard(office)));
}

document.addEventListener("DOMContentLoaded", async () => {
  try {
    await render();
  } catch (error) {
    const servicesNode = document.getElementById("services");
    servicesNode.innerHTML = `<article class="card card-error">${escapeHtml(error.message)}</article>`;
  }
});
