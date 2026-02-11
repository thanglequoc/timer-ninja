// ==============================================
// Particles — Timer Ninja
// Lightweight canvas particle effect for hero
// ==============================================

(function () {
  'use strict';

  var canvas = document.getElementById('heroParticles');
  if (!canvas) return;

  var ctx = canvas.getContext('2d');
  var particles = [];
  var PARTICLE_COUNT = 60;
  var CONNECTION_DISTANCE = 120;
  var animationId;

  function isDark() {
    return document.documentElement.getAttribute('data-theme') === 'dark';
  }

  function getColor(alpha) {
    return isDark()
      ? 'rgba(109, 213, 219, ' + alpha + ')'
      : 'rgba(70, 191, 198, ' + alpha + ')';
  }

  function resize() {
    var hero = canvas.parentElement;
    canvas.width = hero.offsetWidth;
    canvas.height = hero.offsetHeight;
  }

  function Particle() {
    this.x = Math.random() * canvas.width;
    this.y = Math.random() * canvas.height;
    this.vx = (Math.random() - 0.5) * 0.4;
    this.vy = (Math.random() - 0.5) * 0.4;
    this.radius = Math.random() * 2.5 + 0.8;
    this.alpha = Math.random() * 0.4 + 0.15;
  }

  function createParticles() {
    particles = [];
    for (var i = 0; i < PARTICLE_COUNT; i++) {
      particles.push(new Particle());
    }
  }

  function drawParticles() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Draw connections
    for (var i = 0; i < particles.length; i++) {
      for (var j = i + 1; j < particles.length; j++) {
        var dx = particles[i].x - particles[j].x;
        var dy = particles[i].y - particles[j].y;
        var dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < CONNECTION_DISTANCE) {
          var alpha = (1 - dist / CONNECTION_DISTANCE) * 0.15;
          ctx.beginPath();
          ctx.strokeStyle = getColor(alpha);
          ctx.lineWidth = 0.6;
          ctx.moveTo(particles[i].x, particles[i].y);
          ctx.lineTo(particles[j].x, particles[j].y);
          ctx.stroke();
        }
      }
    }

    // Draw particles
    for (var k = 0; k < particles.length; k++) {
      var p = particles[k];
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
      ctx.fillStyle = getColor(p.alpha);
      ctx.fill();

      // Update position
      p.x += p.vx;
      p.y += p.vy;

      // Bounce off edges
      if (p.x < 0 || p.x > canvas.width) p.vx *= -1;
      if (p.y < 0 || p.y > canvas.height) p.vy *= -1;
    }

    animationId = requestAnimationFrame(drawParticles);
  }

  // Pause when not visible
  var heroSection = canvas.parentElement;

  function checkVisibility() {
    var rect = heroSection.getBoundingClientRect();
    var isVisible = rect.bottom > 0 && rect.top < window.innerHeight;

    if (isVisible && !animationId) {
      animationId = requestAnimationFrame(drawParticles);
    } else if (!isVisible && animationId) {
      cancelAnimationFrame(animationId);
      animationId = null;
    }
  }

  // Init
  resize();
  createParticles();
  drawParticles();

  window.addEventListener('resize', function () {
    resize();
    createParticles();
  });

  window.addEventListener('scroll', checkVisibility, { passive: true });
})();
