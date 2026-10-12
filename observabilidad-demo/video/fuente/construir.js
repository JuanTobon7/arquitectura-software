// Construye el video: escena -> HTML -> PNG (Edge) ; narración -> WAV (voz de Windows) ; todo -> MP4 (ffmpeg).
// Uso: node construir.js <salida.mp4>
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');

const DIR = __dirname;
const EDGE = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const escenas = require('./escenas');
const salida = path.resolve(process.argv[2]);
const recortes = { 'panel-tomcat.png': [0, 0, 1600, 780], 'panel-sql.png': [0, 0, 1600, 780], 'alertas.png': [0, 0, 1600, 380], 'alertas-reto.png': [0, 0, 1600, 380],
  'explore-latencia.png': [485, 795, 1420, 400], 'explore-nmas1.png': [485, 690, 1420, 560], 'explore-errores.png': [485, 690, 1420, 560] };

for (const d of ['html', 'frames', 'audio', 'seg']) fs.mkdirSync(path.join(DIR, d), { recursive: true });

const esc = (s) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
const CSS = `
* { margin: 0; box-sizing: border-box; }
body { width: 1920px; height: 1080px; font-family: "Segoe UI", Arial, sans-serif; overflow: hidden; }
.azul { background: linear-gradient(135deg, #003f8a 0%, #005fc2 60%, #1a7fe0 100%); color: #fff; }
.claro { background: #f4f7fb; color: #0b1f3a; }
.etiqueta { font-size: 30px; font-weight: 700; letter-spacing: 3px; text-transform: uppercase; color: #ffb347; }
.claro .etiqueta { color: #005fc2; }
h1 { font-size: 96px; font-weight: 800; line-height: 1.05; }
h2 { font-size: 64px; font-weight: 800; margin: 18px 0 40px; }
.marco { position: absolute; inset: 56px 110px 190px; display: flex; flex-direction: column; }
ul { list-style: none; padding: 0; }
li { font-size: 46px; margin: 26px 0; padding-left: 70px; position: relative; }
li::before { content: ""; position: absolute; left: 0; top: 14px; width: 34px; height: 34px; border-radius: 50%; background: #ffb347; }
.term { flex: 1; background: #0d1117; border-radius: 18px; padding: 40px 48px; font-family: Consolas, monospace; font-size: 30px; line-height: 1.5; color: #e6edf3; box-shadow: 0 20px 60px rgba(0,0,0,.35); overflow: hidden; }
.term .barra { display: flex; gap: 14px; margin: -14px 0 26px; }
.term .barra span { width: 20px; height: 20px; border-radius: 50%; background: #ff5f56; }
.term .barra span:nth-child(2) { background: #ffbd2e; } .term .barra span:nth-child(3) { background: #27c93f; }
.p { color: #7ee787; } .c { color: #8b949e; font-style: italic; } .cmd { color: #ffffff; font-weight: 600; }
.captura { flex: 1; display: flex; align-items: center; justify-content: center; overflow: hidden; border-radius: 14px; background: #fff; box-shadow: 0 16px 50px rgba(0,40,100,.25); }
.captura img { max-width: 100%; max-height: 100%; object-fit: contain; }
.nota { margin-top: 24px; font-size: 34px; font-weight: 600; color: #0b1f3a; background: #fff; border-left: 10px solid #005fc2; padding: 16px 26px; border-radius: 8px; }
ul.densa li { font-size: 40px; margin: 16px 0; }
.num { position: absolute; right: 60px; bottom: 30px; font-size: 26px; opacity: .6; }
`;

function html(e, i) {
  const pie = `<div class="num">${i + 1} / ${escenas.length}</div>`;
  if (e.tipo === 'portada') {
    return `<body class="azul"><div class="marco" style="justify-content:center">
      <div class="etiqueta">${esc(e.pie)}</div>
      <h1 style="margin:30px 0 24px">${esc(e.titulo)}</h1>
      <div style="font-size:52px;color:#cfe3ff">${esc(e.sub)}</div></div>${pie}</body>`;
  }
  if (e.tipo === 'lista') {
    return `<body class="azul"><div class="marco" style="justify-content:center">
      <div class="etiqueta">${esc(e.etiqueta)}</div><h2>${esc(e.titulo)}</h2>
      <ul class="${e.items.length > 4 ? 'densa' : ''}">${e.items.map((t) => `<li>${esc(t)}</li>`).join('')}</ul></div>${pie}</body>`;
  }
  if (e.tipo === 'terminal') {
    const lineas = e.lineas.map(([p, t]) => p === '$' ? `<div><span class="p">$ </span><span class="cmd">${esc(t)}</span></div>`
      : p === '#' ? `<div class="c"># ${esc(t)}</div>` : `<div>${esc(t).replace(/ /g, '&nbsp;')}</div>`).join('');
    return `<body class="claro"><div class="marco"><div class="etiqueta">${esc(e.etiqueta)}</div>
      <div style="height:30px"></div><div class="term"><div class="barra"><span></span><span></span><span></span></div>${lineas}</div></div>${pie}</body>`;
  }
  // captura real
  const src = 'file:///' + path.join(DIR, 'shots', recortes[e.img] ? 'r-' + e.img : e.img).replace(/\\/g, '/');
  const img = `<img src="${src}">`;
  return `<body class="claro"><div class="marco"><div class="etiqueta">${esc(e.etiqueta)}</div>
    <div style="height:24px"></div><div class="captura">${img}</div>
    <div class="nota">${esc(e.nota)}</div></div>${pie}</body>`;
}

// 0) Recortes de las capturas
const Jimp = require('jimp');
(async () => {
for (const [f, [x, y, w, h]] of Object.entries(recortes)) {
  const im = await Jimp.read(path.join(DIR, 'shots', f));
  await im.crop(x, y, Math.min(w, im.bitmap.width - x), Math.min(h, im.bitmap.height - y)).writeAsync(path.join(DIR, 'shots', 'r-' + f));
}

// 1) Imágenes
escenas.forEach((e, i) => {
  const file = path.join(DIR, 'html', `${e.id}.html`);
  fs.writeFileSync(file, `<!doctype html><html lang="es"><head><meta charset="utf-8"><style>${CSS}</style></head>${html(e, i)}</html>`);
  try {
    execFileSync(EDGE, ['--headless=new', '--disable-gpu', '--hide-scrollbars', '--window-size=1920,1080', '--virtual-time-budget=3000',
      `--screenshot=${path.join(DIR, 'frames', `${e.id}.png`)}`, 'file:///' + file.replace(/\\/g, '/')], { stdio: 'ignore' });
  } catch (err) { /* Edge devuelve código distinto de 0 aunque escriba la imagen */ }
});
console.log('imágenes listas');

// 2) Narración con la voz en español de Windows
// Narración. Por defecto usa la voz en español de Windows (Microsoft Sabina, ya incluida en el sistema).
// Con TTS=piper usa Piper (voz neural local y gratuita): PIPER_PYTHON, PIPER_MODEL y PIPER_LENGTH.
if ((process.env.TTS || 'windows') === 'piper') {
  const PY = process.env.PIPER_PYTHON || 'python';
  const MODELO = process.env.PIPER_MODEL || 'es_MX-claude-high.onnx';
  for (const e of escenas) {
    const wav = path.join(DIR, 'audio', `${e.id}.wav`);
    execFileSync(PY, ['-m', 'piper', '-m', MODELO, '-f', wav, '--sentence-silence', '0.35', '--length-scale', process.env.PIPER_LENGTH || '1.0'],
      { input: e.voz, stdio: ['pipe', 'ignore', 'ignore'], env: { ...process.env, PYTHONUTF8: '1', PYTHONIOENCODING: 'utf-8' } });
  }
} else {
  const voces = escenas.map((e) => ({ id: e.id, texto: e.voz, wav: path.join(DIR, 'audio', `${e.id}.wav`) }));
  fs.writeFileSync(path.join(DIR, 'voces.json'), JSON.stringify(voces), 'utf8');
  const ps = [
    'Add-Type -AssemblyName System.Speech',
    `$v = Get-Content -Raw -Encoding UTF8 '${path.join(DIR, 'voces.json')}' | ConvertFrom-Json`,
    '$s = New-Object System.Speech.Synthesis.SpeechSynthesizer',
    "$s.SelectVoice('Microsoft Sabina Desktop')",
    `$s.Rate = ${process.env.WIN_RATE || '1'}`,
    'foreach ($x in $v) { $s.SetOutputToWaveFile($x.wav); $s.Speak($x.texto) }',
    '$s.SetOutputToNull()',
  ].join('\n');
  fs.writeFileSync(path.join(DIR, 'voz.ps1'), '﻿' + ps, 'utf8');
  execFileSync('powershell.exe', ['-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', path.join(DIR, 'voz.ps1')], { stdio: 'inherit' });
}
console.log('narración lista');

// 3) Segmentos de video + subtítulos
const dur = (f) => parseFloat(execFileSync('ffprobe', ['-v', 'error', '-show_entries', 'format=duration', '-of', 'csv=p=0', f], { encoding: 'utf8' }));
const PAUSA = 0.8;
let t = 0;
const srt = [];
const lista = [];
const ts = (s) => { const ms = Math.round(s * 1000); const h = Math.floor(ms / 3600000), m = Math.floor(ms / 60000) % 60, sec = Math.floor(ms / 1000) % 60; return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')},${String(ms % 1000).padStart(3, '0')}`; };
escenas.forEach((e, i) => {
  const wav = path.join(DIR, 'audio', `${e.id}.wav`);
  const d = dur(wav) + PAUSA;
  const seg = path.join(DIR, 'seg', `${e.id}.mp4`);
  execFileSync('ffmpeg', ['-y', '-v', 'error', '-loop', '1', '-framerate', '25', '-i', path.join(DIR, 'frames', `${e.id}.png`), '-i', wav,
    '-af', `apad=pad_dur=${PAUSA}`, '-t', d.toFixed(2), '-c:v', 'libx264', '-tune', 'stillimage', '-pix_fmt', 'yuv420p', '-r', '25',
    '-c:a', 'aac', '-b:a', '128k', '-ar', '44100', '-ac', '2', seg]);
  lista.push(`file '${seg.replace(/\\/g, '/')}'`);
  // subtítulos: partir la narración en frases y repartir el tiempo según su longitud
  const frases = e.voz.match(/[^.!?:]+[.!?:]?/g).map((f) => f.trim()).filter(Boolean)
    .flatMap((f) => f.length <= 95 ? [f] : f.split(/(?<=,) /).reduce((acc, p) => { const u = acc[acc.length - 1]; if (u && (u + ' ' + p).length <= 95) acc[acc.length - 1] = u + ' ' + p; else acc.push(p); return acc; }, []));
  const total = frases.reduce((a, f) => a + f.length, 0);
  let c = t;
  for (const f of frases) {
    const dd = (d - PAUSA) * f.length / total;
    srt.push(`${srt.length + 1}\n${ts(c)} --> ${ts(c + dd)}\n${f}\n`);
    c += dd;
  }
  t += d;
});
fs.writeFileSync(path.join(DIR, 'lista.txt'), lista.join('\n'));
const srtPath = salida.replace(/\.mp4$/, '.srt');
fs.writeFileSync(srtPath, srt.join('\n'), 'utf8');

// 4) Unir y quemar subtítulos
const tmp = path.join(DIR, 'sin-subs.mp4');
execFileSync('ffmpeg', ['-y', '-v', 'error', '-f', 'concat', '-safe', '0', '-i', path.join(DIR, 'lista.txt'), '-c', 'copy', tmp]);
fs.copyFileSync(srtPath, path.join(DIR, 'subs.srt'));
execFileSync('ffmpeg', ['-y', '-v', 'error', '-i', 'sin-subs.mp4', '-vf',
  "subtitles=subs.srt:force_style='FontName=Segoe UI,FontSize=17,PrimaryColour=&H00FFFFFF,BackColour=&H99000000,BorderStyle=4,Outline=0,MarginV=18'",
  '-c:v', 'libx264', '-preset', 'medium', '-crf', '23', '-pix_fmt', 'yuv420p', '-c:a', 'copy', '-movflags', '+faststart', salida], { cwd: DIR });
console.log(`video listo: ${salida} (${t.toFixed(0)} s)`);
})();
