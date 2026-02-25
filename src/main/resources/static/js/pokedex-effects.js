document.addEventListener('DOMContentLoaded', () => {
    const savedTheme = localStorage.getItem('pokedexTheme');
    if (savedTheme) {
        setSceneTheme(savedTheme);
    }
});

function setSceneTheme(theme) {
    document.body.classList.remove('theme-night', 'theme-gameboy');
    if (theme !== 'default') {
        document.body.classList.add('theme-' + theme);
    }
    localStorage.setItem('pokedexTheme', theme);
}

function setSpriteStyle(style) {
    const pkmn = document.getElementById('mainPkmn');
    if (!pkmn) return;
    pkmn.classList.remove('sprite-retro', 'sprite-shadow');
    if (style === 'retro') pkmn.classList.add('sprite-retro');
    if (style === 'shadow') pkmn.classList.add('sprite-shadow');
}

let myChart = null;

function updateBg() {
    const select = document.getElementById('region-select');
    const container = document.getElementById('region-bg');
    if (select && container) {
        container.className = 'filter-section bg-' + select.value;
    }
}

function shutDownTV() {
    const wrapper = document.getElementById('pokedexWrapper');
    const form = document.getElementById('logoutForm');

    document.body.style.backgroundColor = "#000";
    document.body.style.backgroundImage = "none";

    if (wrapper) {
        wrapper.classList.add('tv-off-animation');
    }

    setTimeout(() => {
        if (form) {
            form.submit();
        }
    }, 600);
}

function captureAnimation(event, element) {
    event.preventDefault();
    const url = element.getAttribute('href');
    const card = element.closest('.pokemon-card');
    const img = card ? card.querySelector('.pokemon-img') : null;

    document.body.style.backgroundColor = "#fff";

    if (img) {
        img.style.transition = "all 0.5s ease-in";
        img.style.transform = "scale(0.1) rotate(360deg)";
        img.style.filter = "brightness(5) sepia(1) hue-rotate(100deg)";
        img.style.opacity = "0";
    }

    setTimeout(() => {
        window.location.href = url;
    }, 500);
}

function capturarEnBatalla(btn) {
    const ballContainer = document.getElementById('pokeball-container');
    const ball = document.getElementById('animating-ball');
    const pokemonSprite = document.getElementById('mainPkmn') || document.querySelector('.pokemon-sprite-battle') || document.querySelector('.pokemon-img');
    const dialogText = document.getElementById('dialogText');
    const infoPanel = document.getElementById('infoPanel');
    
    const id = btn.getAttribute('data-id');
    const nombre = btn.getAttribute('data-nombre');
    const urlImg = btn.getAttribute('data-img');

    if (infoPanel !== null && infoPanel !== undefined) {
        if (infoPanel.classList.contains('expanded')) {
            toggleDetails(); 
        }
    }

    if (ballContainer && ball) {
        ballContainer.style.display = 'block';
        ball.classList.add('throw-animation');
    }

    const formData = new FormData();
    formData.append('id', id);
    formData.append('nombre', nombre);
    formData.append('urlImagen', urlImg);

    fetch('/pokedex/guardar', { method: 'POST', body: formData })
    .then(response => response.text())
    .then(data => {
        if (data === "OK") {
            setTimeout(() => {
                if (pokemonSprite) {
                    pokemonSprite.style.transition = '0.5s';
                    pokemonSprite.style.opacity = '0';
                    pokemonSprite.style.transform = 'scale(0.1) rotate(360deg)';
                }
                
                if (ball) {
                    ball.classList.remove('throw-animation');
                    ball.classList.add('capture-shake');
                }

                setTimeout(() => {
                    if (dialogText) {
                        dialogText.innerText = "¡Gotcha! " + nombre.toUpperCase() + " capturado.";
                    }
                    btn.innerText = "ATRAPADO";
                    btn.disabled = true;
                    btn.style.backgroundColor = "#585858";
                    
                    setTimeout(() => {
                        if (ballContainer) ballContainer.style.opacity = '0';
                    }, 1000);
                }, 1200);
            }, 800);
        } else {
            alert("Error: " + data);
            if (ballContainer) ballContainer.style.display = 'none';
        }
    })
    .catch(err => console.error(err));
}

function eliminarDeFavoritos(btn) {
    const idPokemon = btn.getAttribute('data-id');
    const card = btn.closest('.col') || btn.closest('.pokemon-card'); 

    const formData = new FormData();
    formData.append('idPokemon', idPokemon);

    fetch('/pokedex/eliminar', {
        method: 'POST',
        body: formData
    })
    .then(response => response.text())
    .then(data => {
        if (data === "OK") {
            let captured = JSON.parse(localStorage.getItem('myCapturedPkmn')) || [];
            captured = captured.filter(id => id !== idPokemon.toString());
            localStorage.setItem('myCapturedPkmn', JSON.stringify(captured));

            if (card) {
                card.style.transition = "0.5s";
                card.style.opacity = "0";
                card.style.transform = "scale(0.8)";
                setTimeout(() => card.remove(), 500); 
            }
        } else {
            alert("Error al liberar: " + data);
        }
    })
    .catch(error => console.error('Error:', error));
}

function toggleDetails() {
    const info = document.getElementById('infoPanel');
    const extra = document.getElementById('extraData');
    if (!info || !extra) return;

    info.classList.toggle('expanded');
    const isExpanded = info.classList.contains('expanded');
    extra.style.display = isExpanded ? 'block' : 'none';
    
    if (isExpanded) {
        setTimeout(initChart, 50);
    }
}

function switchTab(tabId) {
    document.querySelectorAll('.mini-content').forEach(c => c.classList.remove('active-content'));
    document.querySelectorAll('.btn-tab-mini').forEach(b => b.classList.remove('active'));
    
    const targetTab = document.getElementById(tabId);
    if (targetTab) {
        targetTab.classList.add('active-content');
        event.target.classList.add('active');
        if (tabId === 'miniStats') setTimeout(initChart, 10);
    }
}

function changeMainModel(url, isShiny) {
    const mainPkmn = document.getElementById('mainPkmn');
    const nameLabel = document.getElementById('displayName');
    if (mainPkmn) {
        mainPkmn.style.opacity = '0';
        setTimeout(() => {
            mainPkmn.src = url;
            mainPkmn.style.opacity = '1';
            if (nameLabel) {
                nameLabel.style.color = isShiny ? '#b08800' : '#333';
            }
        }, 200);
    }
}

function initChart() {
    const ctx = document.getElementById('statsChart');
    if (!ctx) return;
    
    if (myChart) myChart.destroy();

    myChart = new Chart(ctx.getContext('2d'), {
        type: 'radar',
        data: {
            labels: ['HP', 'ATK', 'DEF', 'SAT', 'SDF', 'SPD'],
            datasets: [{
                data: statsData,
                backgroundColor: 'rgba(56, 176, 168, 0.4)',
                borderColor: '#38b0a8',
                borderWidth: 2,
                pointRadius: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                r: {
                    angleLines: { color: '#585858' },
                    grid: { color: '#585858' },
                    pointLabels: { font: { family: 'Press Start 2P', size: 6 } },
                    suggestedMin: 0,
                    suggestedMax: 150,
                    ticks: { display: false }
                }
            },
            plugins: { legend: { display: false } }
        }
    });
}