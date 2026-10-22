const currencyFormatter = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD'
});

function formatCurrency(value) {
  const amount = Number(value) || 0;
  return currencyFormatter.format(amount);
}

function showToast(message, type = 'success') {
  const existing = document.querySelector('.toast');
  if (existing) existing.remove();

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;

  const style = document.createElement('style');
  style.textContent = `
    .toast {
      position: fixed;
      right: 24px;
      bottom: 24px;
      z-index: 60;
      padding: 12px 16px;
      border-radius: 12px;
      color: #fff;
      font-weight: 600;
      box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18);
      opacity: 0;
      transform: translateY(10px);
      animation: toastIn 0.25s ease forwards;
    }
    .toast.success { background: #15803d; }
    .toast.error { background: #dc2626; }
    @keyframes toastIn {
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }
  `;

  document.head.appendChild(style);
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.remove();
    style.remove();
  }, 2600);
}

function setLoadingState(button, loading) {
  const label = button.querySelector('.btn-label');
  const spinner = button.querySelector('.spinner');

  if (!label || !spinner) return;

  label.style.opacity = loading ? '0.2' : '1';
  spinner.classList.toggle('hidden', !loading);
  button.disabled = loading;
}

function togglePasswordVisibility(button) {
  const targetId = button.dataset.target;
  const input = document.getElementById(targetId);
  if (!input) return;

  const nextType = input.type === 'password' ? 'text' : 'password';
  input.type = nextType;
  button.textContent = nextType === 'password' ? 'Show' : 'Hide';
}

function bindPasswordToggles() {
  document.querySelectorAll('.toggle-password').forEach((button) => {
    button.addEventListener('click', () => togglePasswordVisibility(button));
  });
}

function getDemoAccounts() {
  return [
    { id: 'ACC-1001', name: 'Primary Checking', balance: 2510.75, owner: 'demo-user', status: 'Active' },
    { id: 'ACC-1002', name: 'Savings', balance: 8420.15, owner: 'demo-user', status: 'Active' }
  ];
}

function getDemoTransactions() {
  return [
    { id: 'TXN-001', accountNumber: 'ACC-1001', type: 'DEPOSIT', date: '2026-10-18', amount: 1500, status: 'Completed' },
    { id: 'TXN-002', accountNumber: 'ACC-1002', type: 'WITHDRAWAL', date: '2026-10-20', amount: 320.5, status: 'Completed' },
    { id: 'TXN-003', accountNumber: 'ACC-1001', type: 'DEPOSIT', date: '2026-10-21', amount: 250.42, status: 'Completed' }
  ];
}

function setupSidebarInteractions() {
  const navItems = document.querySelectorAll('.nav-item');
  navItems.forEach((item) => {
    item.addEventListener('click', () => {
      navItems.forEach((nav) => nav.classList.remove('active'));
      if (!item.classList.contains('button-link')) {
        item.classList.add('active');
      }
    });
  });

  document.querySelectorAll('[data-modal]').forEach((button) => {
    button.addEventListener('click', () => {
      const modalId = button.dataset.modal;
      const modal = document.getElementById(modalId);
      if (modal) {
        modal.classList.remove('hidden');
        modal.setAttribute('aria-hidden', 'false');
      }
    });
  });

  document.querySelectorAll('.close-modal, [data-close-modal]').forEach((el) => {
    el.addEventListener('click', () => {
      const modalId = el.dataset.closeModal || el.closest('.modal')?.id;
      const modal = modalId ? document.getElementById(modalId) : null;
      if (modal) {
        modal.classList.add('hidden');
        modal.setAttribute('aria-hidden', 'true');
      }
    });
  });

  document.querySelectorAll('.modal').forEach((modal) => {
    modal.addEventListener('click', (event) => {
      if (event.target === modal) {
        modal.classList.add('hidden');
        modal.setAttribute('aria-hidden', 'true');
      }
    });
  });

  const logoutButton = document.getElementById('logoutButton');
  if (logoutButton) {
    logoutButton.addEventListener('click', () => {
      window.location.href = 'login.html';
    });
  }
}

function initAuthAndNavigation() {
  bindPasswordToggles();
  setupSidebarInteractions();
}

document.addEventListener('DOMContentLoaded', () => {
  initAuthAndNavigation();
});
