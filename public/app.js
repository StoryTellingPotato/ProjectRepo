// Mock Data for Storage (Ingredients)
// We use localStorage to persist data across multiple pages
function getIngredients() {
    const stored = localStorage.getItem('tribots_ingredients');
    if (stored) return JSON.parse(stored);
    
    const initial = [
        { id: 1, name: "Arabica Beans (1kg)", category: "Coffee Beans", supplier: "Local Bean Co.", price: 850.00, stock: 15, unit: "bags" },
        { id: 2, name: "Vanilla Syrup", category: "Syrups & Sauces", supplier: "Sweet Treats Bakery", price: 350.00, stock: 8, unit: "bottles" },
        { id: 3, name: "Whole Milk", category: "Dairy & Milk", supplier: "Local Bean Co.", price: 95.00, stock: 30, unit: "cartons" },
        { id: 4, name: "Paper Cups (12oz)", category: "Packaging", supplier: "Global Mugs Inc.", price: 2.50, stock: 500, unit: "pcs" },
        { id: 5, name: "Caramel Sauce", category: "Syrups & Sauces", supplier: "Sweet Treats Bakery", price: 420.00, stock: 0, unit: "bottles" }
    ];
    localStorage.setItem('tribots_ingredients', JSON.stringify(initial));
    return initial;
}

function saveIngredients(data) {
    localStorage.setItem('tribots_ingredients', JSON.stringify(data));
}

function getDeliveries() {
    const stored = localStorage.getItem('tribots_deliveries');
    if (stored) return JSON.parse(stored);
    const initial = [
        { id: "ORD-001", ingredient: "Oat Milk", supplier: "Local Bean Co.", amount: "50 cartons", date: "2026-05-12", cost: 6500.00, status: "In Transit" },
        { id: "ORD-002", ingredient: "Robusta Beans", supplier: "Global Mugs Inc.", amount: "20 bags", date: "2026-05-14", cost: 12000.00, status: "Processing" }
    ];
    localStorage.setItem('tribots_deliveries', JSON.stringify(initial));
    return initial;
}

function saveDeliveries(data) {
    localStorage.setItem('tribots_deliveries', JSON.stringify(data));
}

// Determine which page we are on based on existing elements
const isDashboard = document.getElementById('dashboardTableBody') !== null;
const isStorage = document.getElementById('storageTableBody') !== null;
const isIncoming = document.getElementById('incomingTableBody') !== null;
const isStockIn = document.getElementById('stockInForm') !== null;
const isStockOut = document.getElementById('stockOutForm') !== null;

// Toast System
function showToast(message, type = 'success') {
    const toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) return;
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = type === 'success' 
        ? '<i class="fa-solid fa-circle-check"></i>' 
        : '<i class="fa-solid fa-circle-exclamation"></i>';
        
    toast.innerHTML = `${icon} <span>${message}</span>`;
    toastContainer.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'fadeOut 0.3s ease forwards';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// =================== DASHBOARD LOGIC ===================
if (isDashboard) {
    const ingredients = getIngredients();
    
    // Stats
    const totalStock = ingredients.reduce((sum, item) => sum + item.stock, 0);
    const currentItems = ingredients.filter(item => item.stock > 0).length;
    const inventoryValue = ingredients.reduce((sum, item) => sum + (item.price * item.stock), 0);
    
    document.getElementById('totalStock').textContent = totalStock;
    document.getElementById('currentItems').textContent = currentItems;
    document.getElementById('inventoryValue').textContent = `₱${inventoryValue.toLocaleString(undefined, {minimumFractionDigits: 2})}`;

    // Table
    const tableBody = document.getElementById('dashboardTableBody');
    function renderDashboard(data) {
        tableBody.innerHTML = '';
        if (data.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 2rem;">No ingredients found.</td></tr>`;
            return;
        }
        data.forEach(item => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${item.id.toString().padStart(4, '0')}</td>
                <td style="font-weight: 600; color: var(--primary);">${item.name}</td>
                <td style="color: var(--text-muted);">${item.category}</td>
                <td>${item.supplier}</td>
                <td>₱${item.price.toFixed(2)}</td>
                <td><strong>${item.stock}</strong> ${item.unit}</td>
            `;
            tableBody.appendChild(row);
        });
    }
    renderDashboard(ingredients);
    
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const term = e.target.value.toLowerCase();
            renderDashboard(ingredients.filter(p => p.name.toLowerCase().includes(term) || p.category.toLowerCase().includes(term)));
        });
    }
}

// =================== STORAGE LOGIC ===================
if (isStorage) {
    let ingredients = getIngredients();
    const tableBody = document.getElementById('storageTableBody');
    
    function renderStorage(data) {
        tableBody.innerHTML = '';
        if (data.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 2rem;">No ingredients found.</td></tr>`;
            return;
        }
        data.forEach(item => {
            let statusClass = "status-in-stock";
            let statusText = "In Stock";
            if (item.stock === 0) { statusClass = "status-out-of-stock"; statusText = "Out of Stock"; }
            else if (item.stock <= 10) { statusClass = "status-low-stock"; statusText = "Low Stock"; }

            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${item.id.toString().padStart(4, '0')}</td>
                <td style="font-weight: 600; color: var(--primary);">${item.name}</td>
                <td style="color: var(--text-muted);">${item.category}</td>
                <td>₱${item.price.toFixed(2)}</td>
                <td>${item.stock} ${item.unit}</td>
                <td><span class="status-pill ${statusClass}">${statusText}</span></td>
                <td>
                    <button class="btn btn-icon btn-small" onclick="editIngredient(${item.id})" title="Edit">
                        <i class="fa-solid fa-pen"></i>
                    </button>
                    <button class="btn btn-icon btn-small btn-danger" onclick="deleteIngredient(${item.id})" title="Delete">
                        <i class="fa-solid fa-trash"></i>
                    </button>
                </td>
            `;
            tableBody.appendChild(row);
        });
    }
    renderStorage(ingredients);

    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const term = e.target.value.toLowerCase();
            renderStorage(ingredients.filter(p => p.name.toLowerCase().includes(term) || p.category.toLowerCase().includes(term)));
        });
    }

    // Modal Logic
    const modal = document.getElementById('productModal');
    const productForm = document.getElementById('productForm');
    
    document.getElementById('addProductBtn').addEventListener('click', () => {
        modal.classList.add('active');
        document.getElementById('modalTitle').textContent = 'Add New Ingredient';
        productForm.reset();
        document.getElementById('productId').value = '';
    });
    
    document.getElementById('closeModalBtn').addEventListener('click', () => modal.classList.remove('active'));
    document.getElementById('cancelModalBtn').addEventListener('click', (e) => { e.preventDefault(); modal.classList.remove('active'); });

    productForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const id = document.getElementById('productId').value;
        const name = document.getElementById('productName').value;
        const catSel = document.getElementById('productCategory');
        const category = catSel.options[catSel.selectedIndex].text;
        const supSel = document.getElementById('productSupplier');
        const supplier = supSel.options[supSel.selectedIndex].text;
        const price = parseFloat(document.getElementById('productPrice').value);
        const stock = parseInt(document.getElementById('productStock').value);
        const unit = document.getElementById('productUnit').value;

        if (id) {
            const idx = ingredients.findIndex(i => i.id == id);
            if (idx > -1) {
                ingredients[idx] = { ...ingredients[idx], name, category, supplier, price, stock, unit };
                showToast('Ingredient updated successfully!');
            }
        } else {
            const newId = ingredients.length > 0 ? Math.max(...ingredients.map(i => i.id)) + 1 : 1;
            ingredients.push({ id: newId, name, category, supplier, price, stock, unit });
            showToast('New ingredient added!');
        }
        saveIngredients(ingredients);
        renderStorage(ingredients);
        modal.classList.remove('active');
    });

    window.editIngredient = function(id) {
        const item = ingredients.find(i => i.id === id);
        if (item) {
            document.getElementById('productId').value = item.id;
            document.getElementById('productName').value = item.name;
            document.getElementById('productPrice').value = item.price;
            document.getElementById('productStock').value = item.stock;
            document.getElementById('productUnit').value = item.unit;
            
            const setSel = (selId, text) => {
                const sel = document.getElementById(selId);
                for(let i=0; i<sel.options.length; i++) {
                    if(sel.options[i].text === text) { sel.selectedIndex = i; break; }
                }
            };
            setSel('productCategory', item.category);
            setSel('productSupplier', item.supplier);
            
            document.getElementById('modalTitle').textContent = 'Edit Ingredient';
            modal.classList.add('active');
        }
    };

    window.deleteIngredient = function(id) {
        if (confirm('Delete this ingredient from storage?')) {
            ingredients = ingredients.filter(i => i.id !== id);
            saveIngredients(ingredients);
            renderStorage(ingredients);
            showToast('Ingredient deleted.');
        }
    };
}

// =================== STOCK IN / STOCK OUT LOGIC ===================
if (isStockIn || isStockOut) {
    const ingredients = getIngredients();
    const selectEl = document.getElementById(isStockIn ? 'stockInIngredient' : 'stockOutIngredient');
    
    ingredients.forEach(item => {
        const opt = document.createElement('option');
        opt.value = item.id;
        opt.textContent = `${item.name} (${item.stock} ${item.unit} available)`;
        selectEl.appendChild(opt);
    });

    const form = document.getElementById(isStockIn ? 'stockInForm' : 'stockOutForm');
    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const id = parseInt(selectEl.value);
        const qty = parseInt(document.getElementById(isStockIn ? 'stockInQty' : 'stockOutQty').value);
        
        const idx = ingredients.findIndex(i => i.id === id);
        if (idx > -1) {
            if (isStockIn) {
                ingredients[idx].stock += qty;
                showToast(`Added ${qty} to ${ingredients[idx].name}.`);
            } else {
                if (ingredients[idx].stock < qty) {
                    showToast('Not enough stock available!', 'error');
                    return;
                }
                ingredients[idx].stock -= qty;
                showToast(`Deducted ${qty} from ${ingredients[idx].name}.`);
            }
            saveIngredients(ingredients);
            setTimeout(() => { window.location.href = 'storage.html'; }, 1500);
        }
    });
}

// =================== INCOMING LOGIC ===================
if (isIncoming) {
    let deliveries = getDeliveries();
    const tableBody = document.getElementById('incomingTableBody');

    function renderIncoming(data) {
        tableBody.innerHTML = '';
        if (data.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 2rem;">No incoming deliveries.</td></tr>`;
            return;
        }
        data.forEach(item => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${item.id}</td>
                <td style="font-weight: 600;">${item.ingredient}</td>
                <td style="color: var(--text-muted);"><i class="fa-solid fa-truck"></i> ${item.supplier}</td>
                <td>${item.amount}</td>
                <td>${item.date}</td>
                <td>₱${item.cost.toFixed(2)}</td>
                <td><span class="status-pill status-low-stock">${item.status}</span></td>
            `;
            tableBody.appendChild(row);
        });
    }
    renderIncoming(deliveries);

    const modal = document.getElementById('deliveryModal');
    const form = document.getElementById('deliveryForm');
    
    document.getElementById('addDeliveryBtn').addEventListener('click', () => modal.classList.add('active'));
    document.getElementById('closeDeliveryModalBtn').addEventListener('click', () => modal.classList.remove('active'));
    document.getElementById('cancelDeliveryBtn').addEventListener('click', (e) => { e.preventDefault(); modal.classList.remove('active'); });

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const newDel = {
            id: "ORD-" + Math.floor(Math.random() * 1000).toString().padStart(3, '0'),
            ingredient: document.getElementById('delIngredient').value,
            supplier: document.getElementById('delSupplier').value,
            amount: document.getElementById('delAmount').value,
            date: document.getElementById('delDate').value,
            cost: parseFloat(document.getElementById('delCost').value),
            status: "Processing"
        };
        deliveries.push(newDel);
        saveDeliveries(deliveries);
        renderIncoming(deliveries);
        showToast('Delivery logged successfully!');
        modal.classList.remove('active');
        form.reset();
    });
}
