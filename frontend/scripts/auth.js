function validateLoginForm() {
  const username = document.getElementById('username');
  const password = document.getElementById('password');
  const errors = {
    username: '',
    password: ''
  };

  if (!username.value.trim()) {
    errors.username = 'Username is required.';
  }

  if (!password.value.trim()) {
    errors.password = 'Password is required.';
  }

  document.querySelector('[data-error-for="username"]').textContent = errors.username;
  document.querySelector('[data-error-for="password"]').textContent = errors.password;

  return !errors.username && !errors.password;
}

function validateRegistrationForm() {
  const fullName = document.getElementById('fullName');
  const username = document.getElementById('registerUsername');
  const password = document.getElementById('registerPassword');
  const confirmPassword = document.getElementById('confirmPassword');

  const errors = {
    fullName: '',
    registerUsername: '',
    registerPassword: '',
    confirmPassword: ''
  };

  if (!fullName.value.trim()) {
    errors.fullName = 'Full name is required.';
  }

  if (!username.value.trim() || username.value.trim().length < 3) {
    errors.registerUsername = 'Username must be at least 3 characters long.';
  }

  if (!password.value || password.value.length < 8) {
    errors.registerPassword = 'Password must be at least 8 characters long.';
  }

  if (confirmPassword.value !== password.value) {
    errors.confirmPassword = 'Passwords do not match.';
  }

  document.querySelector('[data-error-for="fullName"]').textContent = errors.fullName;
  document.querySelector('[data-error-for="registerUsername"]').textContent = errors.registerUsername;
  document.querySelector('[data-error-for="registerPassword"]').textContent = errors.registerPassword;
  document.querySelector('[data-error-for="confirmPassword"]').textContent = errors.confirmPassword;

  return !errors.fullName && !errors.registerUsername && !errors.registerPassword && !errors.confirmPassword;
}

function updatePasswordStrength() {
  const passwordInput = document.getElementById('registerPassword');
  const strengthBar = document.querySelector('.strength-bar');
  if (!passwordInput || !strengthBar) return;

  const value = passwordInput.value;
  let score = 0;

  if (value.length >= 8) score += 35;
  if (/[A-Z]/.test(value)) score += 20;
  if (/[0-9]/.test(value)) score += 20;
  if (/[^A-Za-z0-9]/.test(value)) score += 25;

  strengthBar.style.width = `${Math.min(score, 100)}%`;

  if (score < 40) {
    strengthBar.style.background = '#dc2626';
  } else if (score < 70) {
    strengthBar.style.background = '#f59e0b';
  } else {
    strengthBar.style.background = '#15803d';
  }
}

function setupAuthForms() {
  const loginForm = document.getElementById('loginForm');
  if (loginForm) {
    loginForm.addEventListener('submit', (event) => {
      event.preventDefault();
      const loginButton = document.getElementById('loginButton');
      if (!validateLoginForm()) {
        showToast('Please correct the highlighted fields.', 'error');
        return;
      }

      setLoadingState(loginButton, true);
      setTimeout(() => {
        setLoadingState(loginButton, false);
        window.location.href = 'dashboard.html';
      }, 700);
    });
  }

  const registerForm = document.getElementById('registerForm');
  if (registerForm) {
    const passwordInput = document.getElementById('registerPassword');
    if (passwordInput) {
      passwordInput.addEventListener('input', updatePasswordStrength);
    }

    registerForm.addEventListener('submit', (event) => {
      event.preventDefault();
      const registerButton = document.getElementById('registerButton');
      if (!validateRegistrationForm()) {
        showToast('Please fix the registration errors.', 'error');
        return;
      }

      setLoadingState(registerButton, true);
      setTimeout(() => {
        setLoadingState(registerButton, false);
        showToast('Registration successful. Please log in.', 'success');
        setTimeout(() => { window.location.href = 'login.html'; }, 800);
      }, 700);
    });
  }
}

document.addEventListener('DOMContentLoaded', () => {
  setupAuthForms();
});
