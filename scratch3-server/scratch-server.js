#! /usr/bin/env node
const fs = require('fs');
const util = require('util');
const path = require('path');

const VirtualMachine = require('minecraft-storeys-scratch-vm');
const storeys = require('minecraft-storeys');

const eventBusURL = process.env.storeys_eventBusURL || 'http://localhost:8080';
const code = process.env.code || 'learn.study.m1n3craft';

// override settings in 'minecraft-storeys-scratch-vm'
global.settings = new Map();
global.settings.set('eventBusURL', eventBusURL);
global.settings.set('code', code);

const scriptsFolder = './scripts';
const scratchProjectFolder = './scratch';

const requireUncached = (module) => {
  delete require.cache[require.resolve(module)]
  return require(module)
}

let fsWait = false;
const readFiles = async (folder, callback) => {
  fs.watch(folder, (type, file) => {
    if (file && fs.existsSync(file)) {
      if (fsWait) return;
      fsWait = setTimeout(() => {
        fsWait = false;
      }, 100);
      callback(file);
    }
  });

  const readdir = util.promisify(fs.readdir);
  const items = await readdir(folder);
  for (var i = 0; i < items.length; i++) {
    callback(items[i]);
  }
}

const executeScripts = async () => {
  const minecraft = await new storeys.MinecraftProvider(eventBusURL, code).connect();

  readFiles(scriptsFolder, async script => {
    const scriptName = scriptsFolder + '/' + script;
    try {
      await requireUncached(scriptName)(minecraft);
    } catch (error) {
      console.error('could not execute script', error);
    }
  });
}

const executeScratchProjects = async () => {
  const virtualMachine = new VirtualMachine();

  readFiles(scratchProjectFolder, async script => {
    const projectLocation = path.join(scratchProjectFolder, script);
    try {
      const fileReader = util.promisify(fs.readFile);
      const project = Buffer.from(await fileReader(projectLocation));
      virtualMachine.loadProject(project);
    } catch (error) {
      console.error('could not execute script', error);
    }
  });

  virtualMachine.start();
}

executeScripts();
executeScratchProjects();