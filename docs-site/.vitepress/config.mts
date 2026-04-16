import { defineConfig } from 'vitepress'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const __dirname = dirname(fileURLToPath(import.meta.url))

// Local HTTPS certs (dev only) — skipped on Vercel where .certs/ doesn't exist
const keyPath = resolve(__dirname, '../.certs/key.pem')
const certPath = resolve(__dirname, '../.certs/cert.pem')
const hasLocalCerts = fs.existsSync(keyPath) && fs.existsSync(certPath)

export default defineConfig({
  title: 'Hotel Booking System',
  description: 'Documentation for the Hotel Room Booking System — ITC2205 Final Project',
  srcDir: 'docs',
  vite: {
    server: hasLocalCerts ? {
      https: {
        key: fs.readFileSync(keyPath),
        cert: fs.readFileSync(certPath)
      }
    } : {}
  },
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Getting Started', link: '/getting-started' },
      { text: 'Releases', link: '/releases' },
      { text: 'Guides', link: '/guides/guest' }
    ],
    sidebar: [
      {
        text: 'Getting Started',
        items: [
          { text: 'Installation & Setup', link: '/getting-started' }
        ]
      },
      {
        text: 'User Guides',
        items: [
          { text: 'Guest Mode', link: '/guides/guest' },
          { text: 'User (Guest Account)', link: '/guides/user' },
          { text: 'Receptionist', link: '/guides/reception' },
          { text: 'Manager / Owner', link: '/guides/manager' }
        ]
      },
      {
        text: 'Features',
        items: [
          { text: 'Authentication & Security', link: '/features/authentication' },
          { text: 'Booking System', link: '/features/booking-system' },
          { text: 'CLI Interface', link: '/features/cli-interface' }
        ]
      },
      {
        text: 'Architecture',
        items: [
          { text: 'System Overview', link: '/architecture/overview' },
          { text: 'Data Model', link: '/architecture/data-model' },
          { text: 'File Structure', link: '/architecture/file-structure' }
        ]
      }
    ],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/steveapo/untitled-group-final-project' }
    ],
    search: {
      provider: 'local'
    },
    footer: {
      message: 'ITC2205 — Untitled Group Final Project'
    }
  }
})
