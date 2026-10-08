const fs = require('fs'); 
const lines = fs.readFileSync('C:/Users/This PC/.gemini/antigravity/brain/39844f77-fa77-4f8d-ad0d-4e9dff744d12/.system_generated/logs/transcript_full.jsonl', 'utf8').split('\n'); 
for (const line of lines) { 
  if (!line) continue; 
  try {
    const obj = JSON.parse(line); 
    if (obj.content && obj.content.includes('"openapi":"3.0.1"')) { 
      fs.writeFileSync('d:/betong/openapi.json', obj.content); 
      console.log('done');
      break; 
    }
  } catch(e) {}
}
