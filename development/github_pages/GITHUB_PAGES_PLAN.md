# Timer Ninja — GitHub Pages Planning Document

## Overview

A Jekyll-based GitHub Pages site serving as both a visually striking landing page and full documentation hub for the Timer Ninja library. Deployed from the `docs/` folder on the `main` branch.

---

## Decisions

| Decision | Choice |
|---|---|
| **Tech stack** | Jekyll (GitHub Pages native, no build CI needed) |
| **Theme** | Fully custom (no base theme) for max creative control |
| **Page scope** | Multi-page: Landing + User Guide + Examples + Advanced Usage |
| **Navigation** | Sticky top navbar with logo, page links, GitHub link, day/night toggle |
| **Hero style** | Full-screen with animated gradient/particle background + ninja sloth mascot |
| **Code comparison** | Side-by-side panels (Traditional vs Timer Ninja) |
| **Animations** | Rich — parallax hero, scroll-triggered fade/slide, animated trace output, typing code effect |
| **Mascot usage** | Heavy — hero, CTA section, footer, 404 page |
| **Day/night mode** | CSS custom properties + JS toggle, preference saved in localStorage |
| **Primary color** | `#46bfc6` (light blue) with complementary palette |

---

## Design System

### Color Palette

**Light Mode:**
| Token | Value | Usage |
|---|---|---|
| Primary | `#46bfc6` | Brand color, buttons, links, accents |
| Primary Dark | `#3aa3a9` | Hover states |
| Primary Light | `#6dd5db` | Gradients, glow |
| Background | `#ffffff` | Page background |
| Surface | `#f5fafa` | Section backgrounds |
| Text | `#1a2b3c` | Body text |
| Text Muted | `#5a6b7c` | Secondary text |
| Code BG | `#f0f6f6` | Code block backgrounds |

**Dark Mode:**
| Token | Value | Usage |
|---|---|---|
| Primary | `#46bfc6` | Unchanged |
| Primary Light | `#6dd5db` | Highlighted elements |
| Background | `#0d1520` | Page background |
| Surface | `#14202e` | Section backgrounds |
| Text | `#e8f0f2` | Body text |
| Text Muted | `#8fa3b2` | Secondary text |
| Code BG | `#111d2b` | Code block backgrounds |

### Typography
- **Body:** Inter (Google Fonts)
- **Code:** JetBrains Mono (Google Fonts)

---

## Site Structure

```
docs/
├── _config.yml                # Jekyll configuration
├── _data/
│   └── navigation.yml         # Navbar links
├── _includes/
│   ├── head.html              # Meta, fonts, CSS, theme init script
│   ├── navbar.html            # Sticky navbar with theme toggle
│   └── footer.html            # Footer with mascot
├── _layouts/
│   ├── default.html           # Base layout
│   ├── home.html              # Landing page layout (includes particles + typing JS)
│   └── docs.html              # Documentation layout (sidebar TOC + scrollspy)
├── _sass/
│   ├── _variables.scss        # Design tokens, CSS custom properties
│   ├── _base.scss             # Reset, typography, global styles
│   ├── _navbar.scss           # Sticky nav, hamburger menu
│   ├── _hero.scss             # Hero section, float animation
│   ├── _features.scss         # Feature cards grid
│   ├── _code.scss             # Code panels, trace output, quickstart steps, tabs
│   ├── _docs.scss             # Documentation sidebar + content styles
│   ├── _animations.scss       # Keyframes, scroll-triggered classes
│   ├── _footer.scss           # Footer styles
│   └── _dark-mode.scss        # Dark mode overrides
├── assets/
│   ├── css/main.scss          # SCSS entry point
│   ├── js/
│   │   ├── theme-toggle.js    # Day/night mode + hamburger + nav scroll
│   │   ├── animations.js      # IntersectionObserver scroll reveals + tabs + trace
│   │   ├── particles.js       # Canvas particle system for hero
│   │   ├── typing-effect.js   # Typing animation for code comparison
│   │   └── docs-toc.js        # Auto-generated TOC + scrollspy for docs
│   └── images/
│       └── mascot.png         # Ninja sloth mascot
├── index.html                 # Landing page
├── user-guide.md              # User Guide (from wiki)
├── examples.md                # Examples (from wiki)
├── advanced-usage.md          # Advanced Usage (from wiki)
├── 404.html                   # Custom 404 page
└── Gemfile                    # Jekyll dependencies
```

---

## Landing Page Sections

1. **Hero** — Full-screen animated gradient with canvas particles, floating mascot, tagline, CTA buttons, version badge
2. **Why Timer Ninja?** — 6 feature cards in responsive grid: One Annotation, Visual Call Tree, Block Tracking, Smart Thresholds, Zero Dependencies, Thread-Safe
3. **Before & After** — Side-by-side code comparison with typing animation: 6 lines of boilerplate → 1 annotation
4. **See It In Action** — Terminal-style trace output with line-by-line reveal animation
5. **Quick Start** — 4-step guide with tabbed Maven/Gradle code blocks
6. **Block Tracking Highlight** — Dedicated showcase of `TimerNinjaBlock.measure()` API
7. **CTA** — Final call-to-action with mascot

---

## Documentation Pages

| Page | Source | Layout |
|---|---|---|
| User Guide | `wiki/User-Guide.md` | `docs` (sidebar TOC) |
| Examples | `wiki/Examples.md` | `docs` (sidebar TOC) |
| Advanced Usage | `wiki/Advanced-Usage.md` | `docs` (sidebar TOC) |

Each page includes:
- Auto-generated sidebar TOC from H2/H3 headings
- Scrollspy highlighting current section
- Previous/Next page navigation

---

## Features

### Day/Night Mode
- Toggle button in navbar (sun/moon icon)
- CSS custom properties for all colors
- Persisted in `localStorage`
- Falls back to `prefers-color-scheme` system preference
- Prevents FOUC with inline `<script>` in `<head>`

### Animations
- **Hero particles** — Canvas-based, 60 translucent circles with connecting lines
- **Scroll reveals** — IntersectionObserver: fade-up, fade-left, fade-right, scale-up
- **Staggered grid** — Feature cards animate in with sequential delays
- **Trace reveal** — Console lines appear one by one (120ms intervals)
- **Typing effect** — Code characters type in sequentially with cursor blink
- **Parallax** — Hero mascot and background shift on scroll
- **Float animation** — Mascot gently bobs up and down

### Responsive
- Navbar collapses to hamburger on mobile (< 768px)
- Feature cards: 3 → 2 → 1 column
- Code panels: side-by-side → stacked
- Docs sidebar hidden on mobile (< 1024px)

---

## Deployment

1. Push `docs/` to `main` branch
2. GitHub Settings → Pages → Source: "Deploy from a branch" → `main` / `/docs`
3. Site available at `https://thanglequoc.github.io/timer-ninja/`

### Local Development

```bash
cd docs
bundle install
bundle exec jekyll serve
# → http://localhost:4000/timer-ninja/
```

---

## Verification Checklist

- [ ] Jekyll builds without errors
- [ ] All 4 pages load correctly
- [ ] Dark/light mode toggles properly
- [ ] localStorage persists theme preference
- [ ] Scroll animations fire on all sections
- [ ] Particle canvas renders in hero
- [ ] Typing effect plays on code comparison
- [ ] Trace output reveals line by line
- [ ] Tab switchers work (Maven/Gradle)
- [ ] Navbar hamburger works on mobile
- [ ] Feature cards grid is responsive
- [ ] Code panels stack on mobile
- [ ] Docs sidebar TOC generates correctly
- [ ] Scrollspy highlights active section
- [ ] Previous/Next page navigation works
- [ ] 404 page renders correctly
- [ ] All links resolve (no broken links)
- [ ] Mascot image loads
