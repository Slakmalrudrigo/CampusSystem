const API_BASE = '/api';

// Application State
let studentsData = [];
let bstData = null;
let hashTableData = null;
let queueData = [];
let stackData = [];
let campusData = { nodes: [], edges: [] };
let editingStudentId = null;

document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initEventListeners();
    fetchAllData();
});

// Navigation logic
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    const tabPages = document.querySelectorAll('.tab-page');
    const titleEl = document.getElementById('current-tab-title');
    const subtitleEl = document.getElementById('current-tab-subtitle');

    const subtitles = {
        'tab-students': 'Manage student records with synchronized Data Structures',
        'tab-bst': 'Binary Search Tree visualization and traversals',
        'tab-hashtable': 'O(1) Direct Hash Map Search & Bucket Chaining',
        'tab-queue': 'First-In First-Out (FIFO) Student Service Queue Board',
        'tab-stack': 'Last-In First-Out (LIFO) Action Undo Engine',
        'tab-campus': 'Campus Navigation & Dijkstra Shortest Path Finder',
        'tab-benchmark': 'Search speed comparison across Data Structures'
    };

    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const tabId = item.getAttribute('data-tab');

            navItems.forEach(i => i.classList.remove('active'));
            tabPages.forEach(p => p.classList.remove('active'));

            item.classList.add('active');
            document.getElementById(tabId).classList.add('active');

            const tabName = item.innerText.trim();
            titleEl.innerText = tabName;
            subtitleEl.innerText = subtitles[tabId] || '';

            // Render specific canvas visualizations when tab opened
            if (tabId === 'tab-bst') renderBSTCanvas();
            if (tabId === 'tab-campus') renderCampusCanvas();
            if (tabId === 'tab-benchmark') runBenchmark();
        });
    });
}

// Event Listeners
function initEventListeners() {
    document.getElementById('btn-refresh').addEventListener('click', fetchAllData);
    document.getElementById('btn-undo-global').addEventListener('click', handleUndo);
    document.getElementById('btn-undo-stack').addEventListener('click', handleUndo);

    // Search input
    document.getElementById('search-student-input').addEventListener('input', filterStudentsTable);

    // Modals
    const modalStudent = document.getElementById('modal-student');
    const modalRequest = document.getElementById('modal-request');

    document.getElementById('btn-open-add-modal').addEventListener('click', () => {
        editingStudentId = null;
        document.getElementById('modal-student-title').innerText = 'Add Student Record';
        document.getElementById('form-student').reset();
        document.getElementById('input-student-id').disabled = false;
        modalStudent.classList.add('active');
    });

    document.getElementById('btn-open-request-modal').addEventListener('click', () => {
        populateRequestStudentDropdown();
        modalRequest.classList.add('active');
    });

    document.querySelectorAll('.close-modal, .close-modal-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            modalStudent.classList.remove('active');
            modalRequest.classList.remove('active');
        });
    });

    // Form Submissions
    document.getElementById('form-student').addEventListener('submit', handleStudentFormSubmit);
    document.getElementById('form-request').addEventListener('submit', handleRequestFormSubmit);

    // Service Queue Process
    document.getElementById('btn-process-queue').addEventListener('click', handleProcessQueue);

    // Pathfinding
    document.getElementById('btn-find-path').addEventListener('click', handleFindShortestPath);

    // Benchmark
    document.getElementById('btn-run-benchmark').addEventListener('click', runBenchmark);
}

// API Fetching
async function fetchAllData() {
    try {
        await Promise.all([
            fetchStudents(),
            fetchBST(),
            fetchHashTable(),
            fetchQueue(),
            fetchStack(),
            fetchCampus()
        ]);
        console.log("All system data synchronized.");
    } catch (err) {
        console.error("Error fetching data:", err);
    }
}

async function fetchStudents() {
    const res = await fetch(`${API_BASE}/students`);
    studentsData = await res.json();
    renderStudentsTable(studentsData);
    updateStats();
}

async function fetchBST() {
    const res = await fetch(`${API_BASE}/bst`);
    bstData = await res.json();
    document.getElementById('bst-height').innerText = bstData.height;
    document.getElementById('bst-nodes').innerText = bstData.nodeCount;
    renderBSTCanvas();
}

async function fetchHashTable() {
    const res = await fetch(`${API_BASE}/hashtable`);
    hashTableData = await res.json();
    document.getElementById('ht-collision-badge').innerText = `Collisions: ${hashTableData.collisions}`;
    renderHashTableBuckets();
}

async function fetchQueue() {
    const res = await fetch(`${API_BASE}/requests`);
    queueData = await res.json();
    renderQueueCards();
}

async function fetchStack() {
    const res = await fetch(`${API_BASE}/stack`);
    stackData = await res.json();
    renderStackHistory();
}

async function fetchCampus() {
    const res = await fetch(`${API_BASE}/campus`);
    campusData = await res.json();
    populateCampusDropdowns();
    renderCampusCanvas();
}

// Render Students Table
function renderStudentsTable(data) {
    const tbody = document.getElementById('student-table-body');
    tbody.innerHTML = '';

    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">No student records found.</td></tr>';
        return;
    }

    data.forEach(s => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><code>${s.studentId}</code></td>
            <td><strong>${s.name}</strong></td>
            <td>${s.programme}</td>
            <td>${s.marks.toFixed(1)}</td>
            <td><span class="badge ${s.grade.startsWith('A') ? 'badge-success' : 'badge-info'}">${s.grade}</span></td>
            <td>${s.gpa.toFixed(2)}</td>
            <td style="color:var(--text-muted);">${s.email}</td>
            <td>
                <button class="btn btn-outline" onclick="editStudent('${s.studentId}')" style="padding:4px 8px;font-size:12px;">Edit</button>
                <button class="btn btn-warning" onclick="deleteStudent('${s.studentId}')" style="padding:4px 8px;font-size:12px;background:var(--danger);color:white;">Delete</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function updateStats() {
    document.getElementById('stat-total-students').innerText = studentsData.length;
    if (studentsData.length === 0) return;

    const avg = studentsData.reduce((acc, s) => acc + s.marks, 0) / studentsData.length;
    document.getElementById('stat-avg-marks').innerText = avg.toFixed(1);

    const top = studentsData.reduce((max, s) => s.marks > max.marks ? s : max, studentsData[0]);
    document.getElementById('stat-top-student').innerText = `${top.name} (${top.marks})`;
}

function filterStudentsTable() {
    const query = document.getElementById('search-student-input').value.toLowerCase();
    const filtered = studentsData.filter(s =>
        s.studentId.toLowerCase().includes(query) ||
        s.name.toLowerCase().includes(query) ||
        s.programme.toLowerCase().includes(query)
    );
    renderStudentsTable(filtered);
}

// Student Form Handling
async function handleStudentFormSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('input-student-id').value;
    const name = document.getElementById('input-name').value;
    const programme = document.getElementById('input-programme').value;
    const marks = document.getElementById('input-marks').value;

    const url = editingStudentId ? `${API_BASE}/students/update` : `${API_BASE}/students/add`;
    const payload = `studentId=${encodeURIComponent(id)}&name=${encodeURIComponent(name)}&programme=${encodeURIComponent(programme)}&marks=${encodeURIComponent(marks)}`;

    const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: payload
    });

    const result = await res.json();
    if (result.success) {
        document.getElementById('modal-student').classList.remove('active');
        fetchAllData();
    } else {
        alert(result.message);
    }
}

window.editStudent = function(id) {
    const s = studentsData.find(st => st.studentId === id);
    if (!s) return;
    editingStudentId = id;
    document.getElementById('modal-student-title').innerText = 'Edit Student Record';
    document.getElementById('input-student-id').value = s.studentId;
    document.getElementById('input-student-id').disabled = true;
    document.getElementById('input-name').value = s.name;
    document.getElementById('input-programme').value = s.programme;
    document.getElementById('input-marks').value = s.marks;
    document.getElementById('modal-student').classList.add('active');
};

window.deleteStudent = async function(id) {
    if (!confirm(`Are you sure you want to delete student ${id}?`)) return;
    const res = await fetch(`${API_BASE}/students/delete?id=${encodeURIComponent(id)}`, { method: 'POST' });
    const result = await res.json();
    if (result.success) {
        fetchAllData();
    } else {
        alert(result.message);
    }
};

async function handleUndo() {
    const res = await fetch(`${API_BASE}/undo`, { method: 'POST' });
    const result = await res.json();
    if (result.success) {
        fetchAllData();
    } else {
        alert(result.message);
    }
}

// Render BST Canvas
function renderBSTCanvas() {
    const canvas = document.getElementById('bst-canvas');
    if (!canvas || !bstData || !bstData.tree) return;
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    drawBSTNode(ctx, bstData.tree, canvas.width / 2, 50, canvas.width / 4);
}

function drawBSTNode(ctx, node, x, y, xOffset) {
    if (!node) return;

    if (node.left) {
        ctx.beginPath();
        ctx.moveTo(x, y);
        ctx.lineTo(x - xOffset, y + 70);
        ctx.strokeStyle = '#23314d';
        ctx.lineWidth = 2;
        ctx.stroke();
        drawBSTNode(ctx, node.left, x - xOffset, y + 70, xOffset / 2);
    }

    if (node.right) {
        ctx.beginPath();
        ctx.moveTo(x, y);
        ctx.lineTo(x + xOffset, y + 70);
        ctx.strokeStyle = '#23314d';
        ctx.lineWidth = 2;
        ctx.stroke();
        drawBSTNode(ctx, node.right, x + xOffset, y + 70, xOffset / 2);
    }

    // Node Circle
    ctx.beginPath();
    ctx.arc(x, y, 24, 0, 2 * Math.PI);
    ctx.fillStyle = '#6366f1';
    ctx.fill();
    ctx.strokeStyle = '#06b6d4';
    ctx.lineWidth = 2;
    ctx.stroke();

    // Text
    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 12px Outfit';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(node.id, x, y);
}

// Hash Table Buckets
function renderHashTableBuckets() {
    const container = document.getElementById('hashtable-buckets');
    container.innerHTML = '';
    if (!hashTableData || !hashTableData.table) return;

    hashTableData.table.forEach(b => {
        const div = document.createElement('div');
        div.className = 'bucket-card';
        let itemsHtml = b.chain.length === 0 ? '<span style="color:var(--text-muted);font-size:12px;">Empty</span>' :
            b.chain.map(c => `<div class="chain-item">[${c.id}] ${c.name}</div>`).join('');

        div.innerHTML = `
            <div class="bucket-header">
                <span>Bucket [${b.index}]</span>
                <span>${b.chain.length} items</span>
            </div>
            <div class="chain-list">${itemsHtml}</div>
        `;
        container.appendChild(div);
    });
}

// Queue Board
function renderQueueCards() {
    const container = document.getElementById('queue-cards-list');
    container.innerHTML = '';

    if (queueData.length === 0) {
        container.innerHTML = '<div style="color:var(--text-muted);padding:16px;">Queue is empty. No pending tickets.</div>';
        return;
    }

    queueData.forEach((q, idx) => {
        const div = document.createElement('div');
        div.className = 'queue-card';
        div.innerHTML = `
            <div>
                <strong>#${idx + 1} Ticket ${q.requestId}</strong>
                <div style="font-size:13px;color:var(--accent);margin-top:2px;">Student: ${q.studentId} | Cat: ${q.category}</div>
                <div style="font-size:13px;color:var(--text-muted);margin-top:4px;">${q.description}</div>
            </div>
            <span class="badge badge-warning">${q.status}</span>
        `;
        container.appendChild(div);
    });
}

function populateRequestStudentDropdown() {
    const sel = document.getElementById('input-req-student-id');
    sel.innerHTML = studentsData.map(s => `<option value="${s.studentId}">${s.studentId} - ${s.name}</option>`).join('');
}

async function handleRequestFormSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('input-req-student-id').value;
    const cat = document.getElementById('input-req-category').value;
    const desc = document.getElementById('input-req-desc').value;

    const payload = `studentId=${encodeURIComponent(id)}&category=${encodeURIComponent(cat)}&description=${encodeURIComponent(desc)}`;
    const res = await fetch(`${API_BASE}/requests`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: payload
    });

    const result = await res.json();
    if (result.success) {
        document.getElementById('modal-request').classList.remove('active');
        fetchQueue();
    }
}

async function handleProcessQueue() {
    const res = await fetch(`${API_BASE}/requests/dequeue`, { method: 'POST' });
    const result = await res.json();
    if (result.success) {
        alert(`Processed Ticket ${result.processed.requestId} for Student ${result.processed.studentId}`);
        fetchQueue();
    } else {
        alert(result.message);
    }
}

// Stack List
function renderStackHistory() {
    const container = document.getElementById('stack-history-list');
    container.innerHTML = '';
    if (stackData.length === 0) {
        container.innerHTML = '<div style="color:var(--text-muted);padding:16px;">Stack is empty. No recent actions.</div>';
        return;
    }

    stackData.forEach((item, idx) => {
        const div = document.createElement('div');
        div.className = 'stack-item';
        div.innerText = `#${idx + 1} [${item.timestamp}] ${item.type}: Student ${item.studentId} (${item.name})`;
        container.appendChild(div);
    });
}

// Campus Canvas & Pathfinding
function populateCampusDropdowns() {
    const startSel = document.getElementById('select-start-loc');
    const targetSel = document.getElementById('select-target-loc');
    if (!campusData.nodes) return;

    const opts = campusData.nodes.map(n => `<option value="${n.id}">${n.id}</option>`).join('');
    startSel.innerHTML = opts;
    targetSel.innerHTML = opts;
    if (campusData.nodes.length > 1) {
        targetSel.selectedIndex = 1;
    }
}

let activePathNodes = [];

function renderCampusCanvas() {
    const canvas = document.getElementById('campus-canvas');
    if (!canvas || !campusData.nodes) return;
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    const positions = {};
    const total = campusData.nodes.length;
    const cx = canvas.width / 2;
    const cy = canvas.height / 2;
    const radius = 200;

    campusData.nodes.forEach((n, idx) => {
        const angle = (idx / total) * 2 * Math.PI;
        positions[n.id] = {
            x: cx + radius * Math.cos(angle),
            y: cy + radius * Math.sin(angle)
        };
    });

    // Draw Edges
    campusData.edges.forEach(e => {
        const p1 = positions[e.source];
        const p2 = positions[e.target];
        if (!p1 || !p2) return;

        const isHighlighted = activePathNodes.includes(e.source) && activePathNodes.includes(e.target) &&
            Math.abs(activePathNodes.indexOf(e.source) - activePathNodes.indexOf(e.target)) === 1;

        ctx.beginPath();
        ctx.moveTo(p1.x, p1.y);
        ctx.lineTo(p2.x, p2.y);
        ctx.strokeStyle = isHighlighted ? '#10b981' : '#23314d';
        ctx.lineWidth = isHighlighted ? 4 : 2;
        ctx.stroke();

        // Edge Distance Label
        const mx = (p1.x + p2.x) / 2;
        const my = (p1.y + p2.y) / 2;
        ctx.fillStyle = '#9ca3af';
        ctx.font = '10px JetBrains Mono';
        ctx.fillText(`${e.distance}m`, mx, my);
    });

    // Draw Nodes
    campusData.nodes.forEach(n => {
        const pos = positions[n.id];
        if (!pos) return;
        const isPath = activePathNodes.includes(n.id);

        ctx.beginPath();
        ctx.arc(pos.x, pos.y, 28, 0, 2 * Math.PI);
        ctx.fillStyle = isPath ? '#10b981' : '#131a2a';
        ctx.fill();
        ctx.strokeStyle = isPath ? '#06b6d4' : '#6366f1';
        ctx.lineWidth = 3;
        ctx.stroke();

        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 11px Outfit';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(n.id, pos.x, pos.y);
    });
}

async function handleFindShortestPath() {
    const start = document.getElementById('select-start-loc').value;
    const target = document.getElementById('select-target-loc').value;

    const res = await fetch(`${API_BASE}/shortest-path?start=${encodeURIComponent(start)}&target=${encodeURIComponent(target)}`);
    const result = await res.json();

    if (result.path && result.path.length > 0) {
        activePathNodes = result.path;
        document.getElementById('path-result-text').innerHTML =
            `<strong>Optimal Dijkstra Route:</strong> ${result.path.join(' ➔ ')} <br><strong>Total Distance:</strong> ${result.totalDistance} meters`;
        renderCampusCanvas();
    } else {
        document.getElementById('path-result-text').innerText = 'No route available between selected buildings.';
    }
}

// Benchmark
async function runBenchmark() {
    const res = await fetch(`${API_BASE}/benchmark?id=STU101`);
    const data = await res.json();

    document.getElementById('bm-list-time').innerText = `${data.linkedListAvgUs.toFixed(4)} μs`;
    document.getElementById('bm-bst-time').innerText = `${data.bstAvgUs.toFixed(4)} μs`;
    document.getElementById('bm-ht-time').innerText = `${data.hashTableAvgUs.toFixed(4)} μs`;
}
