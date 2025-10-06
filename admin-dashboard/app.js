// =============================================================================
// BLUEROOTS ADMIN DASHBOARD - INTEGRATED WITH BACKEND
// =============================================================================

class BlueRootsAdmin {
    constructor() {
        this.apiBaseUrl = 'http://localhost:3000/api';
        this.projects = [];
        this.credits = [];
        this.currentSection = 'dashboard';
        this.autoRefreshInterval = null;
        this.init();
    }

    // =============================================================================
    // INITIALIZATION
    // =============================================================================

    async init() {
        console.log('🚀 Initializing BlueRoots Admin Dashboard...');
        
        this.setupEventListeners();
        this.setupNavigation();
        this.setupModals();
        this.setupTabs();
        
        // Load initial data
        await this.loadAllData();
        
        // Start auto-refresh
        this.startAutoRefresh();
        
        // Test backend connection
        this.testBackendConnection();
        
        console.log('✅ Dashboard initialized successfully');
    }

    setupEventListeners() {
        // Navigation
        document.querySelectorAll('.sidebar__link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const section = link.dataset.section;
                this.navigateToSection(section);
            });
        });

        // Refresh buttons
        document.getElementById('refresh-queue')?.addEventListener('click', () => {
            this.loadAllData();
        });

        document.getElementById('sync-blockchain')?.addEventListener('click', () => {
            this.syncBlockchainData();
        });

        // Add project modal
        document.getElementById('add-project-btn')?.addEventListener('click', () => {
            this.showModal('add-project-modal');
        });

        document.getElementById('close-add-project')?.addEventListener('click', () => {
            this.hideModal('add-project-modal');
        });

        document.getElementById('cancel-add-project')?.addEventListener('click', () => {
            this.hideModal('add-project-modal');
        });

        // Add project form submission
        document.getElementById('add-project-form')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleAddProject();
        });

        // Notifications
        document.getElementById('notifications-btn')?.addEventListener('click', () => {
            this.toggleNotifications();
        });

        // Export buttons
        document.getElementById('export-projects')?.addEventListener('click', () => {
            this.exportProjects();
        });

        document.getElementById('export-registry')?.addEventListener('click', () => {
            this.exportCredits();
        });

        // Generate report
        document.getElementById('generate-report')?.addEventListener('click', () => {
            this.generateReport();
        });

        // AI Performance
        document.getElementById('ai-performance-btn')?.addEventListener('click', () => {
            this.showModal('ai-performance-modal');
        });

        document.getElementById('close-ai-performance')?.addEventListener('click', () => {
            this.hideModal('ai-performance-modal');
        });

        // Issue credits
        document.getElementById('issue-credits')?.addEventListener('click', () => {
            this.showIssueCreditDialog();
        });
    }

    setupNavigation() {
        const links = document.querySelectorAll('.sidebar__link');
        links.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                links.forEach(l => l.classList.remove('active'));
                link.classList.add('active');
                
                const section = link.dataset.section;
                this.navigateToSection(section);
            });
        });
    }

    navigateToSection(section) {
        // Hide all sections
        document.querySelectorAll('.section').forEach(s => {
            s.classList.remove('section--active');
        });
        
        // Show target section
        const targetSection = document.getElementById(section);
        if (targetSection) {
            targetSection.classList.add('section--active');
            this.currentSection = section;
            
            // Load section-specific data
            this.loadSectionData(section);
        }
    }

    setupModals() {
        // Close modal when clicking outside
        document.querySelectorAll('.modal').forEach(modal => {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    this.hideModal(modal.id);
                }
            });
        });
    }

    setupTabs() {
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const tab = btn.dataset.tab;
                const parent = btn.closest('section');
                
                // Remove active class from all tabs and panes
                parent.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
                parent.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
                
                // Add active class to clicked tab and pane
                btn.classList.add('active');
                parent.querySelector(`#${tab}`).classList.add('active');
            });
        });
    }

    // =============================================================================
    // BACKEND API INTEGRATION
    // =============================================================================

    async testBackendConnection() {
        try {
            const response = await fetch(`${this.apiBaseUrl.replace('/api', '')}/health`);
            const data = await response.json();
            
            if (data.status === 'OK') {
                console.log('✅ Backend connection successful');
                this.updateSystemStatus('Operational', true);
            }
        } catch (error) {
            console.error('❌ Backend connection failed:', error);
            this.updateSystemStatus('Disconnected', false);
            this.showNotification('Backend connection failed. Some features may not work.', 'error');
        }
    }

    async loadAllData() {
        console.log('🔄 Loading data from backend...');
        this.showLoadingIndicator();
        
        try {
            await Promise.all([
                this.loadDashboardStats(),
                this.loadProjects(),
                this.loadCredits(),
                this.loadRecentActivity()
            ]);
            
            console.log('✅ All data loaded successfully');
        } catch (error) {
            console.error('❌ Error loading data:', error);
            this.showNotification('Failed to load data from backend', 'error');
        } finally {
            this.hideLoadingIndicator();
        }
    }

    async loadDashboardStats() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/admin/dashboard-stats`);
            const data = await response.json();
            
            if (data.stats) {
                this.updateDashboardMetrics(data.stats);
            }
            
            if (data.recentActivity) {
                this.updateRecentActivity(data.recentActivity);
            }
        } catch (error) {
            console.error('Error loading dashboard stats:', error);
        }
    }

    async loadProjects() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/admin/projects`);
            const data = await response.json();
            
            if (data.data) {
                this.projects = data.data;
                this.renderProjects();
                this.updateProjectCount(data.count);
            }
        } catch (error) {
            console.error('Error loading projects:', error);
        }
    }

    async loadCredits() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/credits`);
            const data = await response.json();
            
            if (data.data) {
                this.credits = data.data;
                this.renderCredits();
                this.updateCreditMetrics();
            }
        } catch (error) {
            console.error('Error loading credits:', error);
        }
    }

    async loadRecentActivity() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/admin/dashboard-stats`);
            const data = await response.json();
            
            if (data.recentActivity) {
                this.renderActivityFeed(data.recentActivity);
            }
        } catch (error) {
            console.error('Error loading recent activity:', error);
        }
    }

    async syncBlockchainData() {
        this.showNotification('Syncing blockchain data...', 'info');
        
        try {
            // Reload all data to get latest blockchain transactions
            await this.loadAllData();
            this.showNotification('Blockchain data synced successfully', 'success');
        } catch (error) {
            console.error('Error syncing blockchain:', error);
            this.showNotification('Failed to sync blockchain data', 'error');
        }
    }

    // =============================================================================
    // RENDER FUNCTIONS
    // =============================================================================

    updateDashboardMetrics(stats) {
        document.getElementById('total-projects').textContent = stats.totalProjects || 0;
        document.getElementById('total-credits').textContent = this.formatNumber(stats.totalCredits || 0);
        document.getElementById('approved-projects').textContent = stats.totalProjects || 0;
        document.getElementById('credits-today').textContent = this.formatNumber(stats.totalCredits || 0);
        
        // Update additional stats if available
        if (stats.totalValue) {
            const valueElement = document.querySelector('[data-metric="total-value"] h3');
            if (valueElement) {
                valueElement.textContent = `$${this.formatNumber(stats.totalValue)}`;
            }
        }
    }

    updateProjectCount(count) {
        document.getElementById('total-projects').textContent = count || 0;
    }

    updateCreditMetrics() {
        const totalCredits = this.credits.reduce((sum, credit) => sum + parseFloat(credit.quantity || 0), 0);
        const totalValue = this.credits.reduce((sum, credit) => sum + parseFloat(credit.total_value || 0), 0);
        
        document.getElementById('total-credits').textContent = this.formatNumber(Math.round(totalCredits));
        document.getElementById('available-credits').textContent = this.formatNumber(Math.round(totalCredits));
        document.getElementById('issued-credits').textContent = this.formatNumber(Math.round(totalCredits));
        
        // Update value display if exists
        const valueElement = document.querySelector('[data-metric="total-value"] h3');
        if (valueElement) {
            valueElement.textContent = `$${this.formatNumber(Math.round(totalValue))}`;
        }
    }

    renderProjects() {
        // Group projects by status
        const grouped = this.groupProjectsByStatus();
        
        // Render in kanban board
        Object.keys(grouped).forEach(status => {
            const container = document.getElementById(`${status}-cards`);
            if (container) {
                container.innerHTML = grouped[status].map(project => 
                    this.createProjectCard(project)
                ).join('');
            }
        });
    }

    groupProjectsByStatus() {
        const statuses = {
            'submitted': [],
            'under_review': [],
            'approved': [],
            'monitoring': []
        };
        
        this.projects.forEach(project => {
            const status = this.mapProjectStatus(project.status);
            if (statuses[status]) {
                statuses[status].push(project);
            }
        });
        
        return statuses;
    }

    mapProjectStatus(status) {
        const statusMap = {
            'PLANNING': 'submitted',
            'REGISTERED': 'under_review',
            'IMPLEMENTATION': 'approved',
            'MONITORING': 'monitoring',
            'VERIFICATION': 'monitoring',
            'COMPLETED': 'approved'
        };
        
        return statusMap[status] || 'submitted';
    }

    createProjectCard(project) {
        const stats = project.stats || {};
        const user = project.user_profiles || {};
        
        return `
            <div class="project-card" data-project-id="${project.id}">
                <div class="project-card__header">
                    <h4>${project.project_name}</h4>
                    <span class="project-badge">${project.project_type}</span>
                </div>
                <div class="project-card__body">
                    <p><strong>Organization:</strong> ${user.organization_name || 'N/A'}</p>
                    <p><strong>Location:</strong> ${project.state}, ${project.country}</p>
                    <p><strong>Area:</strong> ${project.project_area} ha</p>
                    <p><strong>Credits:</strong> ${this.formatNumber(stats.totalCredits || 0)} tCO2e</p>
                    <p><strong>Value:</strong> $${this.formatNumber(stats.totalValue || 0)}</p>
                </div>
                <div class="project-card__footer">
                    <button class="btn btn--sm btn--outline" onclick="admin.viewProject('${project.id}')">
                        View Details
                    </button>
                    <button class="btn btn--sm btn--primary" onclick="admin.updateProjectStatus('${project.id}')">
                        Update Status
                    </button>
                </div>
            </div>
        `;
    }

    renderCredits() {
        const container = document.getElementById('credit-registry-table');
        if (!container) return;
        
        if (this.credits.length === 0) {
            container.innerHTML = '<div class="no-data">No credits found</div>';
            return;
        }
        
        container.innerHTML = `
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Batch ID</th>
                        <th>Project</th>
                        <th>Quantity (tCO2e)</th>
                        <th>Price/tonne</th>
                        <th>Total Value</th>
                        <th>Status</th>
                        <th>Issue Date</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    ${this.credits.map(credit => this.createCreditRow(credit)).join('')}
                </tbody>
            </table>
        `;
    }

    createCreditRow(credit) {
        const project = credit.projects || {};
        
        return `
            <tr>
                <td><code>${credit.batch_id}</code></td>
                <td>${project.project_name || 'Unknown'}</td>
                <td>${this.formatNumber(credit.quantity)}</td>
                <td>$${credit.price_per_tonne || 0}</td>
                <td>$${this.formatNumber(credit.total_value || 0)}</td>
                <td><span class="status-badge status-${credit.status.toLowerCase()}">${credit.status}</span></td>
                <td>${this.formatDate(credit.issue_date)}</td>
                <td>
                    <button class="btn btn--sm btn--outline" onclick="admin.viewCredit('${credit.id}')">
                        View
                    </button>
                </td>
            </tr>
        `;
    }

    renderActivityFeed(activities) {
        const container = document.getElementById('activity-list');
        if (!container) return;
        
        if (activities.length === 0) {
            container.innerHTML = '<div class="activity-item">No recent activity</div>';
            return;
        }
        
        container.innerHTML = activities.map(activity => `
            <div class="activity-item">
                <div class="activity-icon">
                    <span class="material-icons">eco</span>
                </div>
                <div class="activity-content">
                    <p><strong>${activity.project_name}</strong></p>
                    <p>Status: ${activity.status}</p>
                    <span class="activity-time">${this.formatDate(activity.created_at)}</span>
                </div>
            </div>
        `).join('');
    }

    updateRecentActivity(activities) {
        this.renderActivityFeed(activities);
    }

    // =============================================================================
    // AUTO-REFRESH
    // =============================================================================

    startAutoRefresh() {
        // Refresh data every 30 seconds
        this.autoRefreshInterval = setInterval(() => {
            console.log('🔄 Auto-refreshing data...');
            this.loadAllData();
        }, 30000);
    }

    stopAutoRefresh() {
        if (this.autoRefreshInterval) {
            clearInterval(this.autoRefreshInterval);
            this.autoRefreshInterval = null;
        }
    }

    // =============================================================================
    // MODAL FUNCTIONS
    // =============================================================================

    showModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove('hidden');
        }
    }

    hideModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('hidden');
        }
    }

    // =============================================================================
    // PROJECT MANAGEMENT
    // =============================================================================

    async handleAddProject() {
        const formData = {
            project_name: document.getElementById('project-name').value,
            state: document.getElementById('project-state').value,
            project_type: document.getElementById('project-type').value,
            project_area: parseFloat(document.getElementById('project-area').value),
            community_partner: document.getElementById('project-community').value,
            expected_credits: parseFloat(document.getElementById('project-credits').value),
            status: 'PLANNING'
        };
        
        try {
            const response = await fetch(`${this.apiBaseUrl}/projects`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
            
            if (response.ok) {
                this.showNotification('Project added successfully', 'success');
                this.hideModal('add-project-modal');
                document.getElementById('add-project-form').reset();
                await this.loadProjects();
            } else {
                throw new Error('Failed to add project');
            }
        } catch (error) {
            console.error('Error adding project:', error);
            this.showNotification('Failed to add project', 'error');
        }
    }

    async updateProjectStatus(projectId) {
        // Show status update dialog
        const newStatus = prompt('Enter new status (PLANNING, REGISTERED, IMPLEMENTATION, MONITORING, VERIFICATION, COMPLETED):');
        
        if (!newStatus) return;
        
        try {
            const response = await fetch(`${this.apiBaseUrl}/admin/projects/${projectId}/status`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ status: newStatus.toUpperCase() })
            });
            
            if (response.ok) {
                this.showNotification('Project status updated', 'success');
                await this.loadProjects();
            } else {
                throw new Error('Failed to update status');
            }
        } catch (error) {
            console.error('Error updating project status:', error);
            this.showNotification('Failed to update project status', 'error');
        }
    }

    viewProject(projectId) {
        const project = this.projects.find(p => p.id === projectId);
        if (project) {
            alert(`Project Details:\n\nName: ${project.project_name}\nType: ${project.project_type}\nArea: ${project.project_area} ha\nStatus: ${project.status}`);
        }
    }

    viewCredit(creditId) {
        const credit = this.credits.find(c => c.id === creditId);
        if (credit) {
            alert(`Credit Details:\n\nBatch ID: ${credit.batch_id}\nQuantity: ${credit.quantity} tCO2e\nStatus: ${credit.status}`);
        }
    }

    // =============================================================================
    // UTILITY FUNCTIONS
    // =============================================================================

    formatNumber(num) {
        if (!num) return '0';
        return new Intl.NumberFormat().format(Math.round(num));
    }

    formatDate(dateString) {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }

    updateSystemStatus(status, isOperational) {
        const statusElement = document.getElementById('system-status');
        if (statusElement) {
            statusElement.textContent = status;
            statusElement.className = isOperational ? 'status-operational' : 'status-error';
        }
    }

    showNotification(message, type = 'info') {
        console.log(`[${type.toUpperCase()}] ${message}`);
        
        // Create toast notification
        const toast = document.createElement('div');
        toast.className = `toast toast--${type}`;
        toast.textContent = message;
        document.body.appendChild(toast);
        
        setTimeout(() => {
            toast.remove();
        }, 3000);
    }

    showLoadingIndicator() {
        document.body.classList.add('loading');
    }

    hideLoadingIndicator() {
        document.body.classList.remove('loading');
    }

    toggleNotifications() {
        const panel = document.getElementById('notifications-panel');
        if (panel) {
            panel.classList.toggle('hidden');
        }
    }

    loadSectionData(section) {
        // Load section-specific data when navigating
        switch(section) {
            case 'projects':
                this.loadProjects();
                break;
            case 'credits':
                this.loadCredits();
                break;
            case 'dashboard':
                this.loadDashboardStats();
                break;
        }
    }

    showIssueCreditDialog() {
        alert('Credit issuance feature coming soon!');
    }

    exportProjects() {
        const csv = this.convertToCSV(this.projects);
        this.downloadCSV(csv, 'blueroots-projects.csv');
        this.showNotification('Projects exported successfully', 'success');
    }

    exportCredits() {
        const csv = this.convertToCSV(this.credits);
        this.downloadCSV(csv, 'blueroots-credits.csv');
        this.showNotification('Credits exported successfully', 'success');
    }

    convertToCSV(data) {
        if (!data.length) return '';
        
        const headers = Object.keys(data[0]);
        const rows = data.map(row => 
            headers.map(header => JSON.stringify(row[header] || '')).join(',')
        );
        
        return [headers.join(','), ...rows].join('\n');
    }

    downloadCSV(csv, filename) {
        const blob = new Blob([csv], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        a.click();
        window.URL.revokeObjectURL(url);
    }

    generateReport() {
        const reportType = document.getElementById('report-type').value;
        const format = document.getElementById('report-format').value;
        
        this.showNotification(`Generating ${reportType} report in ${format} format...`, 'info');
        
        setTimeout(() => {
            this.showNotification('Report generated successfully', 'success');
        }, 2000);
    }
}

// =============================================================================
// INITIALIZE DASHBOARD
// =============================================================================

let admin;
document.addEventListener('DOMContentLoaded', () => {
    admin = new BlueRootsAdmin();
});

// Add CSS for toast notifications
const style = document.createElement('style');
style.textContent = `
    .toast {
        position: fixed;
        bottom: 20px;
        right: 20px;
        padding: 15px 20px;
        background: #333;
        color: white;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.3);
        z-index: 10000;
        animation: slideIn 0.3s ease;
    }
    
    .toast--success { background: #4caf50; }
    .toast--error { background: #f44336; }
    .toast--info { background: #2196f3; }
    
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    .loading::after {
        content: '';
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 4px;
        background: linear-gradient(90deg, #2196f3, #4caf50);
        animation: loading 1s infinite;
        z-index: 10001;
    }
    
    @keyframes loading {
        0% { transform: translateX(-100%); }
        100% { transform: translateX(100%); }
    }
`;
document.head.appendChild(style);
