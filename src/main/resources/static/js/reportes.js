// reportes.js: usa helpers de dashboard.js (authenticatedFetch, logoutUser, getUserData)
(function(){
  const API_BASE_URL = "http://localhost:8080";

  const mesInputEl = document.getElementById('mesInput');
  const tinacosListEl = document.getElementById('tinacosList');
  const tinacosStateEl = document.getElementById('tinacosState');
  const formEl = document.getElementById('reporteForm');

  function setDefaultMonth(){
    const hoy = new Date();
    const y = hoy.getFullYear();
    const m = String(hoy.getMonth()+1).padStart(2,'0');
    mesInputEl.value = `${y}-${m}`;
  }

  async function cargarTinacos(){
    try {
      tinacosStateEl.className = 'home-state loading';
      tinacosStateEl.textContent = 'Cargando tinacos...';
      tinacosStateEl.style.display = 'block';
      tinacosListEl.innerHTML = '';

      const res = await authenticatedFetch(`${API_BASE_URL}/tinacos`, { method: 'GET' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      const lista = await res.json();

      if (!lista || lista.length === 0){
        tinacosStateEl.className = 'home-state empty';
        tinacosStateEl.textContent = 'No tienes tinacos.';
        return;
      }

      tinacosStateEl.style.display = 'none';

      lista.forEach((t, idx) => {
        // Esperamos que TinacoResponse tenga: id, nombre, codigoIdentificador
        const row = document.createElement('label');
        row.className = 'option-row';
        row.innerHTML = `
          <input type="radio" name="tinacoId" value="${t.id}" ${idx===0 ? 'checked' : ''} />
          <div>
            <div class="option-title">${t.nombre || 'Tinaco'}</div>
            <div class="option-meta">Código: ${t.codigoIdentificador || '-'}</div>
          </div>
        `;
        tinacosListEl.appendChild(row);
      });

    } catch (e){
      tinacosStateEl.className = 'home-state error';
      tinacosStateEl.textContent = 'Error al cargar tinacos.';
      console.error(e);
    }
  }

  function initForm(){
    formEl.addEventListener('submit', async (e) => {
      e.preventDefault();
      const tinacoSel = formEl.querySelector('input[name="tinacoId"]:checked');
      if (!tinacoSel){
        alert('Selecciona un tinaco');
        return;
      }
      const tinacoId = tinacoSel.value;
      const mesVal = mesInputEl.value; // formato YYYY-MM
      if(!/^[0-9]{4}-[0-9]{2}$/.test(mesVal)){
        alert('Selecciona un mes válido');
        return;
      }
      const [anio, mes] = mesVal.split('-');
      try {
        const url = `${API_BASE_URL}/reportes/tinaco/pdf?tinacoId=${encodeURIComponent(tinacoId)}&anio=${anio}&mes=${parseInt(mes,10)}`;
        const res = await authenticatedFetch(url, { method: 'GET' });
        if (!res.ok) {
          const msg = await res.text().catch(() => 'Error generando el PDF');
          alert(msg);
          return;
        }
        const blob = await res.blob();
        const dl = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = dl;
        a.download = `reporte_tinaco_${tinacoId}_${anio}_${mes}.pdf`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(dl);
      } catch (err) {
        alert('No se pudo descargar el PDF');
      }
    });
  }

  async function init(){
    // Asegura datos de usuario (y autenticación)
    await getUserData();
  setDefaultMonth();
    initForm();
    cargarTinacos();
  }

  if (document.readyState === 'loading'){
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
