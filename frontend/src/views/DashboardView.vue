<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const token = localStorage.getItem("token");
const base = (import.meta.env.VITE_BACKEND_URL || "/api").replace(/\/$/, "");

// Data
const fleetStatus = ref([]);
const orderTracking = ref([]);
const error = ref("");
const success = ref("");

// Forms
const displayVehicleForm = ref(false);
const displayOrderForm = ref(false);
const displayProgressForm = ref(false);

const newVehicle = ref({
  type: "LORRY",
  loadCapacity: 1000,
  yearOfManufacture: "2023-01-01",
  lastOdometer: 0,
  loadTypes: ["NORMAL"]
});

const newOrder = ref({
  vehicleId: null,
  driverId: null, // For simplicity in this demo, we might need to look up ourselves or other drivers
  pickUpLat: 40.7128,
  pickUpLon: -74.0060,
  deliveryLat: 42.3601,
  deliveryLon: -71.0589,
  loadType: "NORMAL",
  departureTime: new Date().toISOString().slice(0, 16),
  arrivalTime: new Date().toISOString().slice(0, 16)
});

const progressUpdate = ref({
  orderId: null,
  lat: 0,
  lon: 0,
  type: "DEPARTURE",
  statusMessage: ""
});

// Current company account ID (needed for driverId in order creation usually, assuming self-dispatch)
const currentCompanyAccountId = ref(null);

onMounted(async () => {
  if (!token) {
    router.push("/login");
    return;
  }
  await fetchMe();
  await refreshData();
});

async function fetchMe() {
  try {
    const res = await fetch(`${base}/auth/me`, {
      headers: { "Authorization": `Bearer ${token}` }
    });
    if (res.ok) {
        const companyAccount = await res.json();
        currentCompanyAccountId.value = companyAccount.id;
        newOrder.value.driverId = companyAccount.id; // Default to self
    }
  } catch (e) {
    console.error("Failed to fetch company account", e);
  }
}

async function refreshData() {
  error.value = "";
  try {
    // 1. Fleet Status
    let res = await fetch(`${base}/fleet/status`, {
      headers: { "Authorization": `Bearer ${token}` }
    });
    if (res.ok) fleetStatus.value = await res.json();

    // 2. Order Tracking
    res = await fetch(`${base}/fleet/tracking`, {
      headers: { "Authorization": `Bearer ${token}` }
    });
    if (res.ok) orderTracking.value = await res.json();

  } catch (e) {
    error.value = "Failed to load data: " + e.message;
  }
}

async function createVehicle() {
  try {
    const res = await fetch(`${base}/vehicles/create`, {
      method: "POST",
      headers: { 
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify(newVehicle.value)
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || "Error creating vehicle");
    
    success.value = `Vehicle created! ID: ${data.vehicleId}`;
    displayVehicleForm.value = false;
    refreshData();
  } catch (e) {
    error.value = e.message;
  }
}

async function createOrder() {
  try {
    const res = await fetch(`${base}/orders/create`, {
      method: "POST",
      headers: { 
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify(newOrder.value)
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || "Error creating order");

    success.value = `Order created! ID: ${data.orderId}`;
    displayOrderForm.value = false;
    refreshData();
  } catch (e) {
    error.value = e.message;
  }
}

async function updateProgress() {
  try {
    const payload = {
        orderId: progressUpdate.value.orderId,
        lat: progressUpdate.value.lat,
        lon: progressUpdate.value.lon,
        type: progressUpdate.value.type,
        description: { status: progressUpdate.value.statusMessage }
    };

    const res = await fetch(`${base}/orders/progress`, {
      method: "POST",
      headers: { 
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    });
    if (!res.ok) throw new Error("Error updating progress");

    success.value = "Progress updated!";
    displayProgressForm.value = false;
    refreshData();
  } catch (e) {
    error.value = e.message;
  }
}

function logout() {
  localStorage.removeItem("token");
  router.push("/login"); // or router.push({ name: '...'}) if named routes used
}
</script>

<template>
  <div class="dashboard">
    <header>
      <h1>Fleet Dashboard</h1>
      <div class="actions">
        <span>Company Account ID: {{ currentCompanyAccountId }}</span>
        <button @click="refreshData">Refresh</button>
        <button @click="logout" class="secondary">Logout</button>
      </div>
    </header>

    <div v-if="error" class="error-banner">{{ error }}</div>
    <div v-if="success" class="success-banner">{{ success }}</div>

    <div class="grid">
      <!-- FLEET STATUS -->
      <div class="card">
        <h3>Fleet Status</h3>
        <div v-if="fleetStatus.length === 0">No vehicles found.</div>
        <table v-else>
          <thead>
            <tr><th>ID</th><th>Type</th><th>Status</th><th>Capacity</th></tr>
          </thead>
          <tbody>
            <tr v-for="v in fleetStatus" :key="v.vehicle_id">
              <td>{{ v.vehicle_id }}</td>
              <td>{{ v.vehicle_type }}</td>
              <td>{{ v.status }}</td>
              <td>{{ v.load_capacity }}</td>
            </tr>
          </tbody>
        </table>
        <button @click="displayVehicleForm = !displayVehicleForm">+ Add Vehicle</button>

        <div v-if="displayVehicleForm" class="form-box">
          <h4>New Vehicle</h4>
          <label>Type: <select v-model="newVehicle.type"><option>LORRY</option><option>VAN</option></select></label>
          <label>Capacity: <input type="number" v-model="newVehicle.loadCapacity"></label>
          <label>Year: <input type="date" v-model="newVehicle.yearOfManufacture"></label>
          <button @click="createVehicle">Save</button>
        </div>
      </div>

      <!-- ACTIVE ORDERS -->
      <div class="card">
        <h3>Active Orders & Tracking</h3>
        <div v-if="orderTracking.length === 0">No active orders.</div>
        <div v-else class="tracking-list">
          <div v-for="o in orderTracking" :key="o.order_id" class="track-item">
            <strong>Order #{{ o.order_id }}</strong> (Vehicle {{ o.vehicle_id }})
            <div>Status: {{ o.order_status }} | Last Event: {{ o.last_progress_event }}</div>
            <div class="loc">Location: {{ o.last_known_location?.value || 'Unknown' }}</div>
            <button class="small" @click="progressUpdate.orderId = o.order_id; displayProgressForm = true">Update</button>
          </div>
        </div>
        
        <button @click="displayOrderForm = !displayOrderForm">+ Create Order</button>

        <div v-if="displayOrderForm" class="form-box">
          <h4>New Order</h4>
          <label>Vehicle ID: <input type="number" v-model="newOrder.vehicleId"></label>
          <label>Pickup (Lat/Lon): <input v-model="newOrder.pickUpLat"> <input v-model="newOrder.pickUpLon"></label>
          <label>Delivery (Lat/Lon): <input v-model="newOrder.deliveryLat"> <input v-model="newOrder.deliveryLon"></label>
          <label>Departure: <input type="datetime-local" v-model="newOrder.departureTime"></label>
          <label>Arrival: <input type="datetime-local" v-model="newOrder.arrivalTime"></label>
          <button @click="createOrder">Dispatch</button>
        </div>
      </div>

       <!-- UPDATE PROGRESS MODAL/AREA -->
       <div v-if="displayProgressForm" class="overlay">
         <div class="modal">
           <h3>Update Order #{{ progressUpdate.orderId }}</h3>
           <label>Type: 
             <select v-model="progressUpdate.type">
                 <option>LOADING</option>
                 <option>DEPARTURE</option>
                 <option>STOPOVER</option>
                 <option>BREAK</option>
                 <option>FUEL</option>
                 <option>STOP</option>
                 <option>INSPECTION</option>
                 <option>DEVIATION</option>
                 <option>BREAKDOWN</option>
                 <option>INTERRUPTION</option>
                 <option>UNLOADING</option>
                 <option>ARRIVAL</option>
                 <option>COMPLETION</option>
            </select>
           </label>
           <label>Lat: <input v-model="progressUpdate.lat"></label>
           <label>Lon: <input v-model="progressUpdate.lon"></label>
           <label>Status Note: <input v-model="progressUpdate.statusMessage"></label>
           <div class="btns">
             <button @click="updateProgress">Submit Update</button>
             <button @click="displayProgressForm = false" class="secondary">Cancel</button>
           </div>
         </div>
       </div>

    </div>
  </div>
</template>

<style scoped>
.dashboard { padding: 20px; max-width: 1200px; margin: 0 auto; color: #333; }
header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 1px solid #ccc; padding-bottom: 10px; }
.grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.card { background: #f9f9f9; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
th, td { text-align: left; padding: 8px; border-bottom: 1px solid #ddd; }
button { background: #007bff; color: white; border: none; padding: 8px 12px; cursor: pointer; border-radius: 4px; margin-right: 5px; }
button.secondary { background: #6c757d; }
button.small { padding: 4px 8px; font-size: 0.8em; margin-top: 5px; }
.form-box { background: #fff; padding: 10px; border: 1px solid #ddd; margin-top: 10px; border-radius: 4px; }
.form-box label { display: block; margin-bottom: 5px; font-size: 0.9em; }
.form-box input, .form-box select { width: 100%; padding: 5px; margin-bottom: 5px; box-sizing: border-box; }
.error-banner { background: #ffebee; color: #c62828; padding: 10px; margin-bottom: 10px; border-radius: 4px; }
.success-banner { background: #e8f5e9; color: #2e7d32; padding: 10px; margin-bottom: 10px; border-radius: 4px; }
.tracking-list { max-height: 300px; overflow-y: auto; }
.track-item { background: white; padding: 10px; margin-bottom: 8px; border-left: 4px solid #007bff; }
.loc { font-size: 0.85em; color: #666; }

.overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; }
.modal { background: white; padding: 20px; border-radius: 8px; width: 300px; }
.modal input, .modal select { width: 100%; margin-bottom: 10px; padding: 5px; display: block; } 
</style>
