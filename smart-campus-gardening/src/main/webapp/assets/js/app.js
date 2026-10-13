/* Smart Campus Gardening - small frontend helpers (no framework). */
(function () {
  'use strict';

  // 1. Live table search: <input data-table-filter="#tableId">
  document.querySelectorAll('[data-table-filter]').forEach(function (input) {
    var table = document.querySelector(input.getAttribute('data-table-filter'));
    if (!table) return;
    input.addEventListener('input', function () {
      var q = input.value.trim().toLowerCase();
      table.querySelectorAll('tbody tr').forEach(function (row) {
        if (row.classList.contains('empty-row')) return;
        row.style.display = row.textContent.toLowerCase().indexOf(q) > -1 ? '' : 'none';
      });
    });
  });

  // 2. Confirmation before destructive actions: <form data-confirm="Delete this garden?">
  document.addEventListener('submit', function (e) {
    var msg = e.target.getAttribute && e.target.getAttribute('data-confirm');
    if (msg && !window.confirm(msg)) e.preventDefault();
  });

  // 3. One modal form for both "add" and "edit".
  //    Buttons carry data-mode ("create"/"update"), data-title and data-<field-name> values.
  var SKIP = ['mode', 'title', 'bsToggle', 'bsTarget'];
  document.addEventListener('show.bs.modal', function (ev) {
    var btn = ev.relatedTarget, modal = ev.target;
    var form = modal.querySelector('form.entity-form');
    if (!form || !btn) return;
    form.reset();
    var action = form.querySelector('input[name="action"]');
    if (action && btn.dataset.mode) action.value = btn.dataset.mode;
    var title = modal.querySelector('.modal-title');
    if (title && btn.dataset.title) title.textContent = btn.dataset.title;
    Object.keys(btn.dataset).forEach(function (k) {
      if (SKIP.indexOf(k) > -1) return;
      var el = form.elements[k];
      if (el) el.value = btn.dataset[k];
    });
    modal.querySelectorAll('[data-show-on]').forEach(function (n) {      // e.g. data-show-on="update"
      n.classList.toggle('d-none', n.getAttribute('data-show-on') !== (btn.dataset.mode || 'create'));
    });
    modal.dispatchEvent(new CustomEvent('entity:filled', { detail: { mode: btn.dataset.mode || 'create' } }));
  });

  // 4. Auto-dismiss success alerts
  document.querySelectorAll('.alert-success.auto-dismiss').forEach(function (a) {
    setTimeout(function () { a.classList.remove('show'); a.classList.add('d-none'); }, 5000);
  });

  // 5. Print button
  document.querySelectorAll('[data-print]').forEach(function (b) { b.addEventListener('click', function () { window.print(); }); });
})();

/* Chart helpers (Chart.js is loaded only on pages that need it) */
window.GREEN_PALETTE = ['#2d6a4f', '#52b788', '#b7e4c7', '#d4a373', '#95d5b2', '#1b4332', '#e9c46a', '#74c69d'];
window.makeChart = function (id, type, labels, values, label, opts) {
  var el = document.getElementById(id);
  if (!el || typeof Chart === 'undefined') return null;
  var round = (type === 'doughnut' || type === 'pie');
  var cfg = {
    type: type,
    data: { labels: labels, datasets: [{
      label: label, data: values,
      backgroundColor: round ? window.GREEN_PALETTE : (type === 'line' ? 'rgba(82,183,136,.2)' : '#52b788'),
      borderColor: '#2d6a4f', borderWidth: round ? 2 : 2, borderRadius: type === 'bar' ? 8 : 0,
      fill: type === 'line', tension: .35
    }] },
    options: Object.assign({ responsive: true, maintainAspectRatio: false,
      plugins: { legend: { display: round, position: 'bottom' } },
      scales: round ? {} : { y: { beginAtZero: true, grid: { color: '#f0ebdd' } }, x: { grid: { display: false } } } }, opts || {})
  };
  return new Chart(el, cfg);
};
