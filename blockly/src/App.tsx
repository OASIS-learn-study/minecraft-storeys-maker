import { useEffect, useState } from "react";

import Blockly from "blockly/core";
import { BlocklyWorkspace } from "./blockly/Workspace";
import { generate } from "./blockly/storeys/code";
import { CodeEditor } from "./monaco/CodeEditor";

// @ts-ignore
import classes from "./app.module.css";

const App = () => {
  const [tab, setTab] = useState(1);
  const [token, setToken] = useState("");
  // @ts-ignore
  const [workspace, setWorkspace] = useState<Blockly.WorkspaceSvg>();
  const [code, setCode] = useState("");

  useEffect(() => {
    async function login() {
      const urlParams = new URL(window.location.href).searchParams;
      const response = await fetch(
        "/login/" + encodeURI(urlParams.get("code") || "")
      );
      setToken(await response.text());
    }
    login();
  }, []);

  if (!token) {
    return <></>;
  }

  return (
    <div className={classes.page}>
      <header className={classes.header}>
        <button onClick={() => setTab(1)}>blockly</button>
        <button onClick={() => setTab(2)}>code</button>
      </header>

      {tab === 1 && (
        <>
          <BlocklyWorkspace
            workspace={workspace}
            onWorkspaceChange={(event, workspace) => {
              if (
                event.type === Blockly.Events.BLOCK_CHANGE &&
                workspace.getBlockById(event.blockId).type ===
                  "when_inside"
              ) {
                fetch("/code/when_inside/" + event.newValue, {
                  headers: {
                    Authorization: `bearer ${token}`,
                  }
                });
              }
              const generatedCode = generate(workspace);
              setCode(generatedCode);
              setWorkspace(workspace);
              let formData = new FormData();
              formData.append("file", new Blob([generatedCode]));
              fetch("/code/upload", {
                method: "POST",
                body: formData,
                headers: {
                  Authorization: `bearer ${token}`,
                },
              });
            }}
          />
          <textarea value={code} className={classes.aside} readOnly />
        </>
      )}
      {tab === 2 && (
        <CodeEditor workspace={workspace}/>
      )}
    </div>
  );
};

export default App;
