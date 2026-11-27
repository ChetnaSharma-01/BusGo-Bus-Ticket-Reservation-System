const API='/api';
const state={
  user:JSON.parse(localStorage.getItem('busgoUser')||'null'),
  adminAuth:sessionStorage.getItem('busgoAdminAuth')||'',
  adminName:sessionStorage.getItem('busgoAdminName')||'',
  selectedTrip:null,seats:[],selectedSeats:new Set(),trips:[]
};

const $=s=>document.querySelector(s);
const $$=s=>[...document.querySelectorAll(s)];
const esc=v=>String(v??'').replace(/[&<>'"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]));
const money=v=>'₹'+Number(v||0).toLocaleString('en-IN');

function toast(message,type=''){
  const el=document.createElement('div');el.className='toast '+type;el.textContent=message;
  $('#toastStack').appendChild(el);setTimeout(()=>el.remove(),3800);
}

async function api(path,options={}){
  const headers={'Content-Type':'application/json',...(options.headers||{})};
  if(options.admin && state.adminAuth) headers.Authorization=state.adminAuth;
  const config={...options,headers};delete config.admin;
  try{
    const res=await fetch(API+path,config);
    const text=await res.text();
    let data=null;try{data=text?JSON.parse(text):null}catch{data=text}
    if(!res.ok){
      if(res.status===401) throw new Error('Authentication required. Please sign in again.');
      if(res.status===403) throw new Error('Access denied. Administrator permission is required.');
      const msg=data?.message||data?.error||data?.detail||(typeof data==='string'?data:`Request failed (${res.status})`);
      throw new Error(msg);
    }
    return data;
  }catch(e){
    if(e.name==='TypeError') throw new Error('Cannot reach Spring Boot. Start the backend on http://localhost:8080.');
    throw e;
  }
}

function localDateInput(){
  const d=new Date();d.setDate(d.getDate()+1);
  const y=d.getFullYear(),m=String(d.getMonth()+1).padStart(2,'0'),day=String(d.getDate()).padStart(2,'0');
  return `${y}-${m}-${day}`;
}
function formatTime(x){if(!x)return'—';return new Date(x).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}
function formatDate(x){if(!x)return'—';return new Date(x+'').toLocaleDateString([], {day:'2-digit',month:'short',year:'numeric'})}
function dateTimeLocalValue(d){const x=new Date(d);const p=n=>String(n).padStart(2,'0');return `${x.getFullYear()}-${p(x.getMonth()+1)}-${p(x.getDate())}T${p(x.getHours())}:${p(x.getMinutes())}`}

function setUser(user){
  state.user=user;
  if(user)localStorage.setItem('busgoUser',JSON.stringify(user));else localStorage.removeItem('busgoUser');
  $('#profileBtn').textContent=user?.name?.split(' ')[0]||'Guest';
}
function setAdmin(auth,name){
  state.adminAuth=auth||'';state.adminName=name||'';
  if(auth){sessionStorage.setItem('busgoAdminAuth',auth);sessionStorage.setItem('busgoAdminName',name||'admin');$('#adminPortal').textContent='Admin dashboard'}
  else{sessionStorage.removeItem('busgoAdminAuth');sessionStorage.removeItem('busgoAdminName');$('#adminPortal').textContent='Admin portal'}
}

function showView(name){
  if(name==='admin' && !state.adminAuth){openAdminLogin();return}
  $$('.view').forEach(v=>v.classList.remove('active-view'));
  $(`#${name}View`).classList.add('active-view');
  $$('.nav-link').forEach(b=>b.classList.toggle('active',b.dataset.view===name));
  window.scrollTo({top:0,behavior:'smooth'});
  if(name==='trips')loadBookings();
  if(name==='admin')loadInventory();
  observeReveals();
}

async function checkApi(){
  try{
    // Public read-only endpoint; this intentionally does not touch admin APIs.
    await api('/trips?source=__health__&destination=__health__&date=2099-01-01');
    $('#apiDot').style.background='var(--green)';$('#apiText').textContent='System online';
  }catch{
    $('#apiDot').style.background='var(--red)';$('#apiText').textContent='Backend offline';
  }
}

function openModal(html){$('#modal').innerHTML=html;$('#modalBackdrop').classList.add('show');}
function closeModal(){$('#modalBackdrop').classList.remove('show');$('#modal').innerHTML='';}
$('#modalBackdrop').onclick=e=>{if(e.target.id==='modalBackdrop')closeModal()};

function openAdminLogin(message=''){
  openModal(`
    <div class="modal-head"><div><span class="mini-label">RESTRICTED ACCESS</span><h2>Administrator sign in</h2></div><button class="close-btn" onclick="closeModal()">×</button></div>
    <div class="modal-body">
      <div class="security-note"><b>Protected area.</b> Creating buses, routes and trips is disabled for passengers. Only the server-authenticated ADMIN role can access these operations.</div>
      ${message?`<div class="security-note" style="margin-top:10px;border-color:rgba(255,102,114,.2);color:#ff9aa2">${esc(message)}</div>`:''}
      <form id="adminLoginForm" class="login-box" style="margin-top:16px">
        <input name="username" autocomplete="username" placeholder="Admin username" required>
        <input name="password" type="password" autocomplete="current-password" placeholder="Admin password" required>
        <button class="primary-btn" type="submit">Authenticate <b>→</b></button>
      </form>
    </div>`);
  $('#adminLoginForm').onsubmit=adminLogin;
}

async function adminLogin(e){
  e.preventDefault();
  const f=new FormData(e.target),username=String(f.get('username')).trim(),password=String(f.get('password'));
  const basic='Basic '+btoa(`${username}:${password}`);
  try{
    const result=await api('/admin/login',{method:'POST',body:JSON.stringify({username,password})});
    if(!result?.authenticated)throw new Error('Invalid administrator credentials.');
    setAdmin(basic,result.username);
    closeModal();toast('Administrator authenticated. Management controls unlocked.','success');showView('admin');
  }catch(err){toast(err.message,'error');}
}

function adminLogout(){
  setAdmin('','');showView('home');toast('Admin session ended.','success');
}

$('#adminPortal').onclick=()=>state.adminAuth?showView('admin'):openAdminLogin();

$('#profileBtn').onclick=()=>{
  if(!state.user){openRegister();return}
  openModal(`
    <div class="modal-head"><div><span class="mini-label">PASSENGER ACCOUNT</span><h2>${esc(state.user.name)}</h2></div><button class="close-btn" onclick="closeModal()">×</button></div>
    <div class="modal-body">
      <div class="booking-summary"><div><span class="muted">EMAIL</span><strong>${esc(state.user.email)}</strong></div><div><span class="muted">USER ID</span><strong>#${state.user.id}</strong></div></div>
      <div class="modal-actions"><button class="ghost-btn" onclick="setUser(null);closeModal();toast('Passenger signed out','success')">Sign out</button></div>
    </div>`);
};

$('#travelDate').value=localDateInput();
$('#swapBtn').onclick=()=>{
  const a=$('#source').value,b=$('#destination').value;$('#source').value=b;$('#destination').value=a;
};
$$('.quick-routes button').forEach(b=>b.onclick=()=>{
  $('#source').value=b.dataset.from;$('#destination').value=b.dataset.to;$('#travelDate').value=localDateInput();$('#searchForm').requestSubmit();
});
$('#modifySearch').onclick=()=>window.scrollTo({top:0,behavior:'smooth'});

$('#searchForm').onsubmit=async e=>{
  e.preventDefault();
  const source=$('#source').value.trim(),destination=$('#destination').value.trim(),date=$('#travelDate').value;
  if(!source||!destination||!date)return;
  $('#resultsSection').classList.remove('hidden');
  $('#results').innerHTML='<div class="empty-state"><div class="empty-icon">◌</div><h3>Finding your buses…</h3><p>Searching published trips for your exact route and date.</p></div>';
  $('#resultsSection').scrollIntoView({behavior:'smooth',block:'start'});
  try{
    // THIS MATCHES TripController exactly:
    // GET /api/trips?source=...&destination=...&date=yyyy-MM-dd
    const data=await api(`/trips?source=${encodeURIComponent(source)}&destination=${encodeURIComponent(destination)}&date=${encodeURIComponent(date)}`);
    state.trips=Array.isArray(data)?data:(Array.isArray(data?.content)?data.content:[]);
    console.debug('[BusGo] Search request:', {source,destination,date,endpoint:`/api/trips?source=${encodeURIComponent(source)}&destination=${encodeURIComponent(destination)}&date=${encodeURIComponent(date)}`});
    console.debug('[BusGo] Search response:', state.trips);
    renderTrips(source,destination,date);
  }catch(err){
    $('#results').innerHTML=`<div class="empty-state"><div class="empty-icon">!</div><h3>Search could not be completed</h3><p>${esc(err.message)}</p><button class="ghost-btn" onclick="window.scrollTo({top:0,behavior:'smooth'})">Try again</button></div>`;
    toast(err.message,'error');
  }
};

function renderTrips(source,destination,date){
  $('#resultsTitle').textContent=`${source} → ${destination} · ${formatDate(date)}`;
  if(!state.trips.length){
    $('#results').innerHTML=`
      <div class="empty-state">
        <div class="empty-icon">⌁</div><h3>No published buses found</h3>
        <p>No trip is scheduled for this route on ${esc(formatDate(date))}. Try another date or route.</p>
      </div>`;
    return;
  }
  $('#results').innerHTML=state.trips.map(t=>`
    <article class="trip-card reveal visible">
      <div class="operator"><div class="bus-avatar">🚌</div><div><h3>${esc(t.operatorName)}</h3><p>${esc(t.busNumber)} · ${esc(String(t.busType||'').replace('_',' '))}</p></div></div>
      <div class="route-time"><div class="time-block"><strong>${formatTime(t.departureTime)}</strong><span>${esc(t.source)}</span></div><div class="route-line"></div><div class="time-block"><strong>${formatTime(t.arrivalTime)}</strong><span>${esc(t.destination)}</span></div></div>
      <div class="fare"><strong>${money(t.fare)}</strong><span>per seat</span><div class="seats-available">${t.availableSeats} seats left</div></div>
      <button class="primary-btn book-btn" onclick="openSeatPicker(${t.tripId})">Select seats <b>→</b></button>
    </article>`).join('');
}

async function openSeatPicker(tripId){
  state.selectedTrip=state.trips.find(t=>Number(t.tripId)===Number(tripId));state.selectedSeats=new Set();
  try{state.seats=await api(`/trips/${tripId}/seats`);openModal(seatModal());}
  catch(e){toast(e.message,'error');}
}
function seatModal(){
  const t=state.selectedTrip,rows=[];
  for(let i=0;i<state.seats.length;i+=4)rows.push(state.seats.slice(i,i+4));
  return `<div class="modal-head"><div><span class="mini-label">STEP 1 · SEAT SELECTION</span><h2>${esc(t.operatorName)}</h2><p class="muted">${esc(t.source)} → ${esc(t.destination)} · ${formatDate(t.departureTime)} · ${formatTime(t.departureTime)}</p></div><button class="close-btn" onclick="closeModal()">×</button></div>
  <div class="modal-body"><div class="seat-layout"><div class="driver">DRIVER ◉</div>
  ${rows.map(row=>`<div class="seat-row">${row.map((s,i)=>`${i===2?'<span></span>':''}<button class="seat" ${s.booked?'disabled':''} onclick="toggleSeat(${s.seatId})">${esc(s.seatNumber)}</button>`).join('')}</div>`).join('')}
  <div class="seat-legend"><span><i class="legend-dot"></i>Available</span><span><i class="legend-dot selected"></i>Selected</span><span><i class="legend-dot booked"></i>Booked</span></div></div>
  <div class="booking-summary"><div><span class="muted">SELECTED</span><strong id="selectedLabel">No seats</strong></div><div><span class="muted">TOTAL</span><strong id="seatTotal">${money(0)}</strong></div></div>
  <div class="modal-actions"><button class="ghost-btn" onclick="closeModal()">Cancel</button><button class="primary-btn" id="continueBooking" disabled onclick="startBooking()">Continue <b>→</b></button></div></div>`;
}
function toggleSeat(id){
  const seat=state.seats.find(s=>Number(s.seatId)===Number(id));if(!seat||seat.booked)return;
  if(state.selectedSeats.has(id))state.selectedSeats.delete(id);else state.selectedSeats.add(id);
  $$('.seat').forEach(b=>{const s=state.seats.find(x=>x.seatNumber===b.textContent);b.classList.toggle('selected',!!s&&state.selectedSeats.has(s.seatId));});
  const selected=state.seats.filter(s=>state.selectedSeats.has(s.seatId));
  $('#selectedLabel').textContent=selected.length?selected.map(s=>s.seatNumber).join(', '):'No seats';
  $('#seatTotal').textContent=money(selected.length*Number(state.selectedTrip.fare));$('#continueBooking').disabled=!selected.length;
}
function startBooking(){if(!state.user){openRegister('Create a passenger account to reserve your selected seats.');return}openModal(confirmModal())}
function confirmModal(){
  const t=state.selectedTrip,seats=state.seats.filter(s=>state.selectedSeats.has(s.seatId));
  return `<div class="modal-head"><div><span class="mini-label">STEP 2 · REVIEW</span><h2>Confirm your journey</h2></div><button class="close-btn" onclick="closeModal()">×</button></div><div class="modal-body">
  <div class="booking-summary"><div><span class="muted">ROUTE</span><strong>${esc(t.source)} → ${esc(t.destination)}</strong></div><div><span class="muted">TOTAL</span><strong>${money(seats.length*t.fare)}</strong></div></div>
  <div class="booking-meta"><div>Bus<strong>${esc(t.busNumber)}</strong></div><div>Seats<strong>${seats.map(s=>s.seatNumber).join(', ')}</strong></div><div>Departure<strong>${formatTime(t.departureTime)}</strong></div></div>
  <div class="modal-actions"><button class="ghost-btn" onclick="openSeatPicker(${t.tripId})">← Seats</button><button class="primary-btn" onclick="createBooking()">Reserve seats <b>→</b></button></div></div>`;
}
async function createBooking(){
  try{
    const booking=await api('/bookings',{method:'POST',body:JSON.stringify({userId:state.user.id,tripId:state.selectedTrip.tripId,seatIds:[...state.selectedSeats]})});
    openModal(paymentModal(booking));
  }catch(e){toast(e.message,'error');if(/seat/i.test(e.message))openSeatPicker(state.selectedTrip.tripId);}
}
function paymentModal(b){
  return `<div class="modal-head"><div><span class="mini-label">STEP 3 · DEMO PAYMENT</span><h2>Complete payment</h2></div><button class="close-btn" onclick="closeModal()">×</button></div><div class="modal-body">
  <div class="booking-summary"><div><span class="muted">BOOKING</span><strong>BG-${b.bookingId}</strong></div><div><span class="muted">AMOUNT</span><strong>${money(b.totalAmount)}</strong></div></div>
  <p class="muted" style="margin:17px 0 8px">Academic demo payment — no real money is charged.</p>
  <select id="paymentMethod" class="payment-select" style="width:100%"><option>UPI</option><option>CARD</option><option>NETBANKING</option></select>
  <div class="modal-actions"><button class="primary-btn" onclick="payBooking(${b.bookingId})">Pay ${money(b.totalAmount)} <b>→</b></button></div></div>`;
}
async function payBooking(id){
  try{
    const p=await api(`/payments/${id}`,{method:'POST',body:JSON.stringify({paymentMethod:$('#paymentMethod').value})});
    const all=await api(`/bookings/user/${state.user.id}`),booking=all.find(x=>Number(x.bookingId)===Number(id));
    openModal(ticketModal(booking,p));toast('Payment successful — ticket confirmed.','success');
  }catch(e){toast(e.message,'error');}
}
function ticketModal(b,p){
  return `<div class="ticket"><div class="ticket-top"><div><div class="ticket-brand">BusGo</div><span class="mini-label">E-TICKET · CONFIRMED</span></div><div class="ticket-check">✓</div></div>
  <h2>Booking confirmed!</h2><div class="ticket-route"><strong>${esc(b.source)}</strong> → <strong>${esc(b.destination)}</strong> · ${formatDate(b.departureTime)}</div>
  <div class="ticket-grid"><div><span>Booking ID</span><strong>BG-${b.bookingId}</strong></div><div><span>Seats</span><strong>${b.seatNumbers.join(', ')}</strong></div><div><span>Departure</span><strong>${formatTime(b.departureTime)}</strong></div><div><span>Status</span><strong>CONFIRMED</strong></div></div>
  <div class="ticket-total"><span>Total paid</span><strong>${money(b.totalAmount)}</strong></div><div class="ticket-ref">Transaction reference · <b>${esc(p.transactionRef)}</b></div>
  <div class="modal-actions"><button class="ghost-btn" onclick="closeModal();showView('trips')">My bookings</button><button class="primary-btn" onclick="window.print()">Print ticket <b>↗</b></button></div></div>`;
}

async function loadBookings(){
  const list=$('#bookingsList');
  if(!state.user){list.innerHTML=`<div class="empty-state"><div class="empty-icon">♙</div><h3>Sign in to view bookings</h3><p>Create a passenger account while booking to keep your reservations here.</p><button class="primary-btn" style="margin:15px auto 0" onclick="openRegister()">Create account <b>→</b></button></div>`;return}
  try{
    const data=await api(`/bookings/user/${state.user.id}`);
    if(!data.length){list.innerHTML=`<div class="empty-state"><div class="empty-icon">⌁</div><h3>No bookings yet</h3><p>Search a route above and choose a bus to start your journey.</p><button class="primary-btn" style="margin:15px auto 0" onclick="showView('home')">Find a bus <b>→</b></button></div>`;return}
    list.innerHTML=data.map(b=>`<article class="booking-card reveal visible"><div><div class="booking-top"><span class="booking-id">BG-${b.bookingId}</span><span class="status ${b.status}">${b.status}</span></div><h3>${esc(b.source)} → ${esc(b.destination)}</h3><div class="booking-route">${formatDate(b.departureTime)} · ${formatTime(b.departureTime)}</div><div class="booking-meta"><div>Seats<strong>${b.seatNumbers.join(', ')}</strong></div><div>Trip<strong>#${b.tripId}</strong></div><div>Total<strong>${money(b.totalAmount)}</strong></div><div>Booked<strong>${formatDate(b.bookingTime)}</strong></div></div></div><div class="booking-actions">${b.status==='PENDING'?`<button class="primary-btn" onclick="resumePayment(${b.bookingId},${b.totalAmount})">Pay now <b>→</b></button>`:''}${b.status!=='CANCELLED'?`<button class="ghost-btn" onclick="cancelBooking(${b.bookingId})">Cancel</button>`:''}</div></article>`).join('');
  }catch(e){list.innerHTML=`<div class="empty-state"><h3>Couldn’t load bookings</h3><p>${esc(e.message)}</p></div>`}
}
function resumePayment(id,amount){openModal(`<div class="modal-head"><div><span class="mini-label">PAYMENT</span><h2>Complete booking</h2></div><button class="close-btn" onclick="closeModal()">×</button></div><div class="modal-body"><div class="booking-summary"><span>Amount due</span><strong>${money(amount)}</strong></div><select id="paymentMethod" class="payment-select" style="width:100%;margin-top:16px"><option>UPI</option><option>CARD</option><option>NETBANKING</option></select><div class="modal-actions"><button class="primary-btn" onclick="payBooking(${id})">Pay now <b>→</b></button></div></div>`)}
async function cancelBooking(id){
  if(!confirm(`Cancel booking BG-${id}? The seats will become available again.`))return;
  try{await api(`/bookings/${id}`,{method:'DELETE'});toast('Booking cancelled and seats released.','success');loadBookings();}
  catch(e){toast(e.message,'error')}
}
function openRegister(note=''){
  openModal(`<div class="modal-head"><div><span class="mini-label">PASSENGER ACCOUNT</span><h2>Create your account</h2></div><button class="close-btn" onclick="closeModal()">×</button></div><div class="modal-body">${note?`<div class="security-note" style="margin-bottom:12px">${esc(note)}</div>`:''}<form id="registerForm" class="register-form"><input name="name" placeholder="Full name" required><input name="email" type="email" placeholder="Email address" required><input name="password" type="password" minlength="6" placeholder="Password (6+ characters)" required><button class="primary-btn" type="submit">Create account <b>→</b></button></form></div>`);
  $('#registerForm').onsubmit=async e=>{
    e.preventDefault();const f=new FormData(e.target);
    try{const user=await api('/users/register',{method:'POST',body:JSON.stringify({name:f.get('name'),email:f.get('email'),password:f.get('password')})});setUser(user);toast(`Welcome, ${user.name.split(' ')[0]}!`,'success');closeModal();if(state.selectedTrip&&state.selectedSeats.size)openModal(confirmModal());}
    catch(err){toast(err.message,'error')}
  };
}

/* ---------- Protected admin area ---------- */
async function loadInventory(){
  if(!state.adminAuth){openAdminLogin();return false}
  try{
    const [buses,routes,trips]=await Promise.all([
      api('/admin/buses',{admin:true}),
      api('/admin/routes',{admin:true}),
      api('/admin/trips',{admin:true})
    ]);
    $('#adminBus').innerHTML='<option value="">Select bus</option>'+buses.map(b=>`<option value="${b.id}">${esc(b.busNumber)} · ${esc(b.operatorName)}</option>`).join('');
    $('#adminRoute').innerHTML='<option value="">Select route</option>'+routes.map(r=>`<option value="${r.id}">${esc(r.source)} → ${esc(r.destination)}</option>`).join('');
    $('#busInventory').innerHTML=buses.length?buses.map(b=>`<div class="inventory-item"><b>#${b.id} · ${esc(b.busNumber)}</b><span>${esc(b.operatorName)} · ${b.totalSeats} seats</span></div>`).join(''):'<p class="muted">No buses yet.</p>';
    $('#routeInventory').innerHTML=routes.length?routes.map(r=>`<div class="inventory-item"><b>#${r.id} · ${esc(r.source)} → ${esc(r.destination)}</b><span>${r.distanceKm} km</span></div>`).join(''):'<p class="muted">No routes yet.</p>';
    $('#tripInventory').innerHTML=trips.length?trips.map(t=>`<div class="inventory-item"><b>#${t.tripId} · ${esc(t.source)} → ${esc(t.destination)}</b><span>${esc(t.busNumber)} · ${formatDate(t.departureTime)} · ${formatTime(t.departureTime)} · ${money(t.fare)} · ${t.availableSeats}/${t.totalSeats} seats</span></div>`).join(''):'<p class="muted">No published trips yet.</p>';
    return true;
  }catch(e){
    if(/Authentication|required|Access denied/i.test(e.message)){setAdmin('','');openAdminLogin('Your admin session expired. Please authenticate again.');}
    else toast(`Inventory could not be loaded: ${e.message}`,'error');
    return false;
  }
}
$('#refreshInventory').onclick=loadInventory;
$('#busForm').onsubmit=async e=>{
  e.preventDefault();if(!state.adminAuth)return openAdminLogin();
  const f=new FormData(e.target);
  try{await api('/admin/buses',{admin:true,method:'POST',body:JSON.stringify({busNumber:f.get('busNumber'),operatorName:f.get('operatorName'),type:f.get('type'),totalSeats:Number(f.get('totalSeats'))})});toast('Bus added successfully.','success');e.target.reset();loadInventory();}
  catch(err){toast(err.message,'error')}
};
$('#routeForm').onsubmit=async e=>{
  e.preventDefault();if(!state.adminAuth)return openAdminLogin();
  const f=new FormData(e.target);
  try{await api('/admin/routes',{admin:true,method:'POST',body:JSON.stringify({source:f.get('source'),destination:f.get('destination'),distanceKm:Number(f.get('distanceKm'))})});toast('Route published successfully.','success');e.target.reset();loadInventory();}
  catch(err){toast(err.message,'error')}
};
$('#tripForm').onsubmit=async e=>{
  e.preventDefault();if(!state.adminAuth)return openAdminLogin();
  const f=new FormData(e.target);
  const payload={busId:Number(f.get('busId')),routeId:Number(f.get('routeId')),departureTime:f.get('departureTime'),arrivalTime:f.get('arrivalTime'),fare:Number(f.get('fare'))};
  try{
    const created=await api('/admin/trips',{admin:true,method:'POST',body:JSON.stringify(payload)});
    const refreshed=await loadInventory();
    if(!refreshed) throw new Error('Trip was created but the admin inventory could not be refreshed. Click Refresh and check the backend console.');
    toast(`Trip #${created?.id??'created'} published and saved to the database. ${payload.departureTime.replace('T',' ')}`,'success');
    e.target.reset();setAdminDefaults();
  }catch(err){toast(`Trip was not published: ${err.message}`,'error')}
};
function setAdminDefaults(){
  const dep=new Date(Date.now()+24*3600000);dep.setHours(9,0,0,0);
  const arr=new Date(dep.getTime()+8*3600000);$('#departureTime').value=dateTimeLocalValue(dep);$('#arrivalTime').value=dateTimeLocalValue(arr);
}

/* ---------- Motion / navigation ---------- */
$$('.nav-link').forEach(b=>b.onclick=()=>showView(b.dataset.view));
function observeReveals(){
  const io=new IntersectionObserver(entries=>entries.forEach(x=>{if(x.isIntersecting){x.target.classList.add('visible');io.unobserve(x.target)}}),{threshold:.08});
  $$('.reveal:not(.visible)').forEach(el=>io.observe(el));
}
document.addEventListener('mousemove',e=>{const g=$('#cursorGlow');if(g){g.style.left=e.clientX+'px';g.style.top=e.clientY+'px'}});

setUser(state.user);
setAdminDefaults();
observeReveals();
checkApi();
