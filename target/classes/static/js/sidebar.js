// sidebar.js - comportamiento común del sidebar (dropdowns)

(function () {
  function closeAllDropdowns(except) {
    document.querySelectorAll('.sidebar .dropdown').forEach((d) => {
      if (except && d === except) return;
      d.classList.remove('active');
      const m = d.querySelector('.dropdown-menu');
      if (m) m.classList.remove('show');
    });
  }

  function initSidebarDropdowns() {
    // Evitar inicializar dos veces
    if (document.body.dataset.sidebarInitialized === 'true') return;
    document.body.dataset.sidebarInitialized = 'true';

    // Click en los toggles de cada dropdown (no cerrar al hacer clic fuera, por requerimiento)
    document.querySelectorAll('.sidebar .dropdown > .dropdown-toggle').forEach((toggle) => {
      toggle.addEventListener('click', (e) => {
        e.preventDefault();
        const dropdown = e.currentTarget.closest('.dropdown');
        const isOpen = dropdown.classList.contains('active');

        // Cerrar todos primero
        closeAllDropdowns();

        // Si no estaba abierto, abrirlo; si ya estaba, se queda cerrado
        if (!isOpen) {
          dropdown.classList.add('active');
          const menu = dropdown.querySelector('.dropdown-menu');
          if (menu) menu.classList.add('show');
        }
      });
    });

    // Botón hamburguesa para abrir/cerrar sidebar en pantallas pequeñas
    const toggleBtn = document.getElementById('navToggle');
    const sidebar = document.getElementById('sidebar');
    if (toggleBtn && sidebar) {
      toggleBtn.addEventListener('click', () => {
        const open = sidebar.classList.toggle('open');
        toggleBtn.setAttribute('aria-expanded', String(open));
      });

      // Cerrar sidebar al hacer clic/tocar fuera del mismo (solo si está abierto)
      document.addEventListener('click', (e) => {
        const isClickInside = sidebar.contains(e.target) || (toggleBtn && toggleBtn.contains(e.target));
        if (!isClickInside && sidebar.classList.contains('open')) {
          sidebar.classList.remove('open');
          toggleBtn.setAttribute('aria-expanded', 'false');
        }
      });

      // Soporte táctil: mismo comportamiento para touchstart
      document.addEventListener('touchstart', (e) => {
        const isTouchInside = sidebar.contains(e.target) || (toggleBtn && toggleBtn.contains(e.target));
        if (!isTouchInside && sidebar.classList.contains('open')) {
          sidebar.classList.remove('open');
          toggleBtn.setAttribute('aria-expanded', 'false');
        }
      }, { passive: true });
    }
  }

  // Inicializar cuando el DOM esté listo
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initSidebarDropdowns);
  } else {
    initSidebarDropdowns();
  }
})();
