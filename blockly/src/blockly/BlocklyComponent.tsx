import { ReactNode, useLayoutEffect, useRef, useState } from "react";

import "blockly/blocks";
import Blockly from "blockly/core";
// @ts-ignore
import locale from "blockly/msg/en";

import initBlocks from "./storeys/blocks";
import initGenerator from "./storeys/code";
// @ts-ignore
import classes from "./blockly.module.css";

type BlocklyComponentProps = {
  initialXml?: string;
  onWorkspaceChange?: (workspace?: Blockly.WorkspaceSvg) => void
  children?: ReactNode;
};

export const BlocklyComponent = ({
  initialXml,
  onWorkspaceChange,
  children,
  ...rest
}: BlocklyComponentProps) => {
  const ref = useRef<HTMLDivElement>(null);
  const toolbox = useRef();
  const [workspace, setWorkspace] = useState<Blockly.WorkspaceSvg>();

  const changeListener = () => {
    if (workspace && onWorkspaceChange) {
      onWorkspaceChange(workspace)
    }
  }

  useLayoutEffect(() => {
    Blockly.setLocale(locale);
    initBlocks();
    initGenerator();
    const w = Blockly.inject(ref.current, {
      toolbox: toolbox.current,
      media:
        "https://unpkg.com/blockly@^8.0.2/media/",
      rendererOverrides: {
        ADD_START_HATS: true,
      },
      ...rest,
    });
    if (initialXml) {
      Blockly.Xml.domToWorkspace(Blockly.Xml.textToDom(initialXml), w);
    }
    if (onWorkspaceChange)
      w.addChangeListener(changeListener)
    return () => workspace?.removeChangeListener(changeListener)

    setWorkspace(w);
  }, []);

  return (
    <>
      <div ref={ref} className={classes.blockly} />
      <xml
        xmlns="https://developers.google.com/blockly/xml"
        is="blockly"
        style={{ display: "none" }}
        ref={toolbox}
      >
        {children}
      </xml>
    </>
  );
};

