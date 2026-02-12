const LOGIN_ENDPOINT = "/api/auth/login";
const SIGNUP_ENDPOINT = "/api/auth/signup";
const CONFIG_ENDPOINT = "/api/auth/config";
let appBaseUrl = "http://localhost:8080";

const form = document.getElementById("loginForm");
const formTitle = document.getElementById("formTitle");
const modeLoginBtn = document.getElementById("modeLoginBtn");
const modeSignupBtn = document.getElementById("modeSignupBtn");
const message = document.getElementById("message");
const submitBtn = document.getElementById("submitBtn");
const actionLinks = Array.from(document.querySelectorAll(".link-btn"));

let mode = "login";

function showMessage(text, type) {
  message.textContent = text;
  message.className = `message ${type}`;
}

function setMode(nextMode) {
  mode = nextMode;
  const isLogin = mode === "login";
  formTitle.textContent = isLogin ? "Connexion MediSante+" : "Creation de compte MediSante+";
  submitBtn.textContent = isLogin ? "Se connecter" : "Creer un compte";
  modeLoginBtn.classList.toggle("active", isLogin);
  modeSignupBtn.classList.toggle("active", !isLogin);
  showMessage("", "");
}

async function postJson(url, payload) {
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });

  const text = await response.text();
  let body = null;
  try {
    body = text ? JSON.parse(text) : null;
  } catch (_error) {
    body = null;
  }

  if (!response.ok) {
    const apiError = body && typeof body.error === "string" ? body.error : text;
    throw new Error(apiError || "Erreur");
  }

  return body;
}

async function loadConfig() {
  try {
    const response = await fetch(CONFIG_ENDPOINT);
    if (!response.ok) {
      return;
    }
    const data = await response.json();
    if (data && typeof data.appBaseUrl === "string" && data.appBaseUrl.trim() !== "") {
      appBaseUrl = data.appBaseUrl.trim().replace(/\/$/, "");
    }
  } catch (_error) {
    // keep default local URL
  }

  actionLinks.forEach((link) => {
    const target = link.getAttribute("data-target");
    if (!target) {
      return;
    }
    link.setAttribute("href", `${appBaseUrl}${target}`);
  });
}

modeLoginBtn.addEventListener("click", () => setMode("login"));
modeSignupBtn.addEventListener("click", () => setMode("signup"));

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  if (!form.checkValidity()) {
    form.reportValidity();
    return;
  }

  const payload = {
    username: form.username.value.trim(),
    password: form.password.value
  };

  submitBtn.disabled = true;
  showMessage(mode === "login" ? "Connexion en cours..." : "Creation du compte en cours...", "");

  try {
    if (mode === "signup") {
      await postJson(SIGNUP_ENDPOINT, payload);
      showMessage("Compte cree. Vous pouvez maintenant vous connecter.", "ok");
      setMode("login");
      form.password.value = "";
      return;
    }

    const data = await postJson(LOGIN_ENDPOINT, payload);
    if (data && data.token) {
      localStorage.setItem("medisante_auth_token", data.token);
    }
    showMessage("Connexion reussie. Redirection...", "ok");
    window.location.href = `${appBaseUrl}/#home`;
  } catch (error) {
    showMessage(error instanceof Error ? error.message : "Erreur", "err");
  } finally {
    submitBtn.disabled = false;
  }
});

loadConfig();
