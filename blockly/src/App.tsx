import { useEffect, useState } from "react";

import Blockly from "blockly/core";

import { BlocklyWorkspace } from "./blockly/Workspace";
import { Toolbar } from "./Toolbar";
import { CodeEditor } from "./monaco/CodeEditor";
import { upload } from "./blockly/upload";
import { generate } from "./blockly/storeys/code";

// @ts-ignore
import classes from "./app.module.css";

const App = () => {
  const [tab, setTab] = useState(1);
  const [token, setToken] = useState("");
  // @ts-ignore
  const [workspace, setWorkspace] = useState<Blockly.WorkspaceSvg>();
  const [code, setCode] = useState("");

  useEffect(() => {
    let theToken: string;
    async function login() {
      const urlParams = new URL(window.location.href).searchParams;
      const response = await fetch(
        "/login/" + encodeURI(urlParams.get("code") || "")
      );
      theToken = await response.text();
      setToken(theToken);
    }
    async function workspace() {
      try {
        const response = await fetch("/code/workspace", {
          headers: {
            Authorization: `bearer ${theToken}`,
          },
        });
        const xml = await response.text();
        setWorkspace(Blockly.Xml.textToDom(xml));
      } catch (error) {
        setWorkspace(
          Blockly.Xml.textToDom(
            '<xml xmlns="http://www.w3.org/1999/xhtml"></xml>'
          )
        );
      }
    }

    login().then(() => workspace());
  }, []);

  if (!workspace) {
    return <>Loading...</>;
  }

  return (
    <div className={classes.page}>
      <Toolbar className={classes.header} onTabSwitch={(tab) => setTab(tab)} />

      {tab === 1 && (
        <>
          <BlocklyWorkspace
            workspace={workspace}
            onWorkspaceChange={(event, workspace) => {
              if (
                event.type === Blockly.Events.BLOCK_CHANGE &&
                workspace.getBlockById(event.blockId).type === "when_inside"
              ) {
                fetch("/code/when_inside/" + event.newValue, {
                  headers: {
                    Authorization: `bearer ${token}`,
                  },
                });
              }
              const generatedCode = generate(workspace);
              setCode(generatedCode);
              setWorkspace(workspace);
              upload(generatedCode, "/code/upload", token);
              upload(
                Blockly.Xml.domToText(Blockly.Xml.workspaceToDom(workspace)),
                // JSON.stringify(
                //Blockly.serialization.workspaces.save(workspace)
                // ),
                "/code/workspace/upload",
                token
              );
            }}
          />
          <textarea value={code} className={classes.aside} readOnly />
        </>
      )}
      {tab === 2 && <CodeEditor workspace={workspace} />}
    </div>
  );
};

export default App;
