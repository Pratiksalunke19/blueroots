const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
require('dotenv').config();

const app = express();

// Import routes
const authRoutes = require('./routes/auth');
const projectRoutes = require('./routes/projects');
const monitoringRoutes = require('./routes/monitoring');
const creditRoutes = require('./routes/credits');
const adminRoutes = require('./routes/admin');

// FIXED: Enhanced CORS configuration
app.use(cors({
    origin: function(origin, callback) {
        // Allow requests with no origin (like mobile apps or curl requests)
        if (!origin) return callback(null, true);
        
        // List of allowed origins
        const allowedOrigins = [
            'http://localhost:3001',
            'http://127.0.0.1:3001',
            'http://192.168.56.1:3001',  // Your specific IP
            'http://localhost:8080',
            'http://10.0.2.2:3000',  // Android emulator
            /^http:\/\/192\.168\.\d+\.\d+:3001$/,  // Allow any local network IP on port 3001
        ];
        
        // Check if origin is allowed
        const isAllowed = allowedOrigins.some(allowed => {
            if (typeof allowed === 'string') {
                return origin === allowed;
            } else if (allowed instanceof RegExp) {
                return allowed.test(origin);
            }
            return false;
        });
        
        if (isAllowed) {
            callback(null, true);
        } else {
            console.log('⚠️ Blocked by CORS:', origin);
            callback(null, true); // For development, allow all origins
            // callback(new Error('Not allowed by CORS')); // Use this in production
        }
    },
    credentials: true,
    methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With'],
}));

// Middleware
app.use(helmet({
    crossOriginResourcePolicy: { policy: "cross-origin" }
}));
app.use(morgan('combined'));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Health check
app.get('/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    message: 'BlueRoots Backend is running',
    timestamp: new Date().toISOString()
  });
});

// Root endpoint
app.get('/', (req, res) => {
  res.json({
    message: 'BlueRoots Carbon Registry API',
    version: '1.0.0',
    endpoints: {
      health: '/health',
      auth: '/api/auth',
      projects: '/api/projects',
      monitoring: '/api/monitoring',
      credits: '/api/credits',
      admin: '/api/admin'
    }
  });
});

// API Routes
app.use('/api/auth', authRoutes);
app.use('/api/projects', projectRoutes);
app.use('/api/monitoring', monitoringRoutes);
app.use('/api/credits', creditRoutes);
app.use('/api/admin', adminRoutes);

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ 
    error: 'Something went wrong!',
    message: process.env.NODE_ENV === 'development' ? err.message : 'Internal server error'
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ 
    error: 'Route not found',
    path: req.path,
    method: req.method
  });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {  // FIXED: Listen on all network interfaces
  console.log(`\n${'='.repeat(50)}`);
  console.log(`🚀 BlueRoots Backend Server`);
  console.log(`${'='.repeat(50)}`);
  console.log(`📡 Server running on: http://localhost:${PORT}`);
  console.log(`🌐 Network access: http://192.168.56.1:${PORT}`);
  console.log(`📊 Admin Dashboard: http://192.168.56.1:3001`);
  console.log(`🔗 Health Check: http://localhost:${PORT}/health`);
  console.log(`🌍 API Base: http://localhost:${PORT}/api`);
  console.log(`${'='.repeat(50)}\n`);
  console.log(`Available endpoints:`);
  console.log(`  GET  /health`);
  console.log(`  GET  /api/auth/check`);
  console.log(`  GET  /api/projects`);
  console.log(`  GET  /api/credits`);
  console.log(`  GET  /api/admin/projects`);
  console.log(`  GET  /api/admin/dashboard-stats`);
  console.log(`${'='.repeat(50)}\n`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('SIGTERM signal received: closing HTTP server');
  process.exit(0);
});

process.on('SIGINT', () => {
  console.log('\nSIGINT signal received: closing HTTP server');
  process.exit(0);
});
