function populateAccountDropdowns() {
  const accounts = getDemoAccounts();
  const selectors = document.querySelectorAll('select[id$="Account"]');

  selectors.forEach((select) => {
    select.innerHTML = '<option value="">Select an account</option>' +
      accounts.map((account) => `<option value="${account.id}">${account.name} (${account.id})</option>`).join('');
  });
}

function renderRecentTransactions() {
  const tableBody = document.getElementById('recentTransactionsTableBody');
  if (!tableBody) return;

  const transactions = getDemoTransactions().slice(0, 4);

  if (!transactions.length) {
    tableBody.innerHTML = '<tr><td colspan="5" class="empty-state-cell">No recent transactions available.</td></tr>';
    return;
  }

  tableBody.innerHTML = transactions.map((tx) => `
    <tr>
      <td>${tx.id}</td>
      <td>${tx.type}</td>
      <td>${new Date(tx.date).toLocaleDateString()}</td>
      <td class="amount ${tx.type === 'DEPOSIT' ? 'positive' : 'negative'}">
        ${tx.type === 'DEPOSIT' ? '+' : '-'}${formatCurrency(tx.amount)}
      </td>
      <td><span class="status-pill success">${tx.status}</span></td>
    </tr>
  `).join('');
}

function renderAccountCards() {
  const grid = document.getElementById('accountGrid');
  if (!grid) return;

  const accounts = getDemoAccounts();

  if (!accounts.length) {
    grid.innerHTML = '<div class="empty-state-card">No accounts available yet. Create your first account to begin.</div>';
    return;
  }

  grid.innerHTML = accounts.map((account) => `
    <article class="account-card-item">
      <div class="account-head">
        <div>
          <p>Account</p>
          <h3>${account.name}</h3>
        </div>
        <span class="status-pill success">${account.status}</span>
      </div>
      <div class="account-number-row">
        <span>Number</span>
        <strong>${account.id.slice(0, 4)} •••• ••••</strong>
      </div>
      <div class="account-meta">
        <div>
          <span>Type</span>
          <strong>Checking</strong>
        </div>
        <div>
          <span>Currency</span>
          <strong>USD</strong>
        </div>
      </div>
      <div class="account-balance-row">
        <span>Balance</span>
        <strong>${formatCurrency(account.balance)}</strong>
      </div>
    </article>
  `).join('');
}

function updateSummaryCards() {
  const accounts = getDemoAccounts();
  const transactions = getDemoTransactions();
  const totalBalance = accounts.reduce((sum, account) => sum + Number(account.balance), 0);
  const totalDeposits = transactions.filter((tx) => tx.type === 'DEPOSIT').reduce((sum, tx) => sum + Number(tx.amount), 0);
  const totalWithdrawals = transactions.filter((tx) => tx.type === 'WITHDRAWAL').reduce((sum, tx) => sum + Number(tx.amount), 0);

  const totalBalanceEl = document.getElementById('totalBalance');
  const accountCountEl = document.getElementById('accountCount');
  const totalDepositsEl = document.getElementById('totalDeposits');
  const totalWithdrawalsEl = document.getElementById('totalWithdrawals');

  if (totalBalanceEl) totalBalanceEl.textContent = formatCurrency(totalBalance);
  if (accountCountEl) accountCountEl.textContent = String(accounts.length);
  if (totalDepositsEl) totalDepositsEl.textContent = formatCurrency(totalDeposits);
  if (totalWithdrawalsEl) totalWithdrawalsEl.textContent = formatCurrency(totalWithdrawals);

  const primaryBalance = document.getElementById('primaryBalance');
  const maskedAccount = document.getElementById('maskedAccountNumber');
  const welcomeUser = document.getElementById('welcomeUser');
  const primaryAccountHolder = document.getElementById('primaryAccountHolder');

  if (primaryBalance) primaryBalance.textContent = formatCurrency(accounts[0]?.balance || 0);
  if (maskedAccount) maskedAccount.textContent = `${accounts[0]?.id.slice(0, 4) || 'ACC-'} •••• ••••`;
  if (welcomeUser) welcomeUser.textContent = 'Welcome, Demo';
  if (primaryAccountHolder) primaryAccountHolder.textContent = 'Demo User';
}

function renderTransactionHistory() {
  const tbody = document.getElementById('allTransactionsBody');
  if (!tbody) return;

  let transactions = getDemoTransactions();
  const search = document.getElementById('transactionSearch');
  const typeFilter = document.getElementById('transactionTypeFilter');
  const dateFilter = document.getElementById('transactionDateFilter');

  if (search && search.value) {
    transactions = transactions.filter((item) => item.id.toLowerCase().includes(search.value.toLowerCase()));
  }

  if (typeFilter && typeFilter.value !== 'all') {
    transactions = transactions.filter((item) => item.type === typeFilter.value);
  }

  if (dateFilter && dateFilter.value) {
    transactions = transactions.filter((item) => item.date === dateFilter.value);
  }

  if (!transactions.length) {
    tbody.innerHTML = '<tr><td colspan="6" class="empty-state-cell">No transaction matches the current filters.</td></tr>';
    return;
  }

  tbody.innerHTML = transactions.map((item) => `
    <tr>
      <td>${item.id}</td>
      <td>${item.accountNumber}</td>
      <td>${item.type}</td>
      <td>${new Date(item.date).toLocaleDateString()}</td>
      <td class="amount ${item.type === 'DEPOSIT' ? 'positive' : 'negative'}">
        ${item.type === 'DEPOSIT' ? '+' : '-'}${formatCurrency(item.amount)}
      </td>
      <td><span class="status-pill success">${item.status}</span></td>
    </tr>
  `).join('');
}

function bindTransactionFilters() {
  const searchInput = document.getElementById('transactionSearch');
  const typeFilter = document.getElementById('transactionTypeFilter');
  const dateFilter = document.getElementById('transactionDateFilter');
  const clearFiltersBtn = document.getElementById('clearFiltersBtn');

  [searchInput, typeFilter, dateFilter].forEach((element) => {
    if (element) {
      element.addEventListener('input', renderTransactionHistory);
      element.addEventListener('change', renderTransactionHistory);
    }
  });

  if (clearFiltersBtn) {
    clearFiltersBtn.addEventListener('click', () => {
      if (searchInput) searchInput.value = '';
      if (typeFilter) typeFilter.value = 'all';
      if (dateFilter) dateFilter.value = '';
      renderTransactionHistory();
    });
  }
}

function bindFormsAndUi() {
  const depositForm = document.getElementById('depositForm');
  const withdrawForm = document.getElementById('withdrawForm');
  const createAccountForm = document.getElementById('createAccountForm');

  if (depositForm) {
    const amountInput = document.getElementById('depositAmount');
    const summaryEl = document.getElementById('depositSummary');

    amountInput?.addEventListener('input', () => {
      summaryEl.textContent = formatCurrency(amountInput.value || 0);
    });

    depositForm.addEventListener('submit', (event) => {
      event.preventDefault();
      const amount = Number(document.getElementById('depositAmount').value);
      if (!amount || amount <= 0) {
        showToast('Amount must be greater than zero.', 'error');
        return;
      }
      showToast('Deposit request prepared for backend validation.', 'success');
      document.querySelector('[data-close-modal="depositModal"]').click();
    });
  }

  if (withdrawForm) {
    const amountInput = document.getElementById('withdrawAmount');
    const summaryEl = document.getElementById('withdrawSummary');

    amountInput?.addEventListener('input', () => {
      summaryEl.textContent = formatCurrency(amountInput.value || 0);
    });

    withdrawForm.addEventListener('submit', (event) => {
      event.preventDefault();
      const amount = Number(document.getElementById('withdrawAmount').value);
      if (!amount || amount <= 0) {
        showToast('Withdrawal amount must be greater than zero.', 'error');
        return;
      }
      showToast('Withdrawal request prepared for backend validation.', 'success');
      document.querySelector('[data-close-modal="withdrawModal"]').click();
    });
  }

  if (createAccountForm) {
    createAccountForm.addEventListener('submit', (event) => {
      event.preventDefault();
      showToast('Account creation submitted for backend processing.', 'success');
      document.querySelector('[data-close-modal="createAccountModal"]').click();
    });
  }
}

document.addEventListener('DOMContentLoaded', () => {
  populateAccountDropdowns();
  renderRecentTransactions();
  renderAccountCards();
  updateSummaryCards();
  renderTransactionHistory();
  bindTransactionFilters();
  bindFormsAndUi();
});
