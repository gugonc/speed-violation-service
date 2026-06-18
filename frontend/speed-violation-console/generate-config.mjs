import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const envPath = path.join(__dirname, '.env');
if (fs.existsSync(envPath)) {
  for (const line of fs.readFileSync(envPath, 'utf8').split('\n')) {
    const eqIdx = line.indexOf('=');
    if (eqIdx < 1) continue;
    const key = line.slice(0, eqIdx).trim();
    const val = line.slice(eqIdx + 1).trim();
    if (key && !process.env[key]) process.env[key] = val;
  }
}

const apiUrl = process.env.API_URL;
if (!apiUrl) {
  console.error('Erro: variável API_URL não definida.');
  console.error('Crie um arquivo .env com API_URL=https://... ou defina a variável de ambiente.');
  process.exit(1);
}

fs.writeFileSync(path.join(__dirname, 'config.js'), `window.__API_URL__ = '${apiUrl}';\n`, 'utf8');
console.log('config.js gerado com API_URL:', apiUrl);
