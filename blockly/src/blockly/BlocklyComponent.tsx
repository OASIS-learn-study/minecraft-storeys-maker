import { ReactNode, useLayoutEffect, useRef } from "react";

import "blockly/blocks";
import * as Blockly from "blockly/core";
import * as locale from "blockly/msg/en";

import initBlocks from "./storeys/blocks";
import initGenerator from "./storeys/code";
// @ts-ignore
import classes from "./blockly.module.css";

type BlocklyComponentProps = {
  workspace?: Element;
  onWorkspaceChange?: (event: any, workspace?: Blockly.WorkspaceSvg) => void;
  children?: ReactNode;
};

function resize() {
  const blocklyArea = document.getElementById("blocklyArea")!;
  const blocklyDiv = document.getElementById("blocklyDiv")!;
  var onresize = function () {
    // Compute the absolute coordinates and dimensions of blocklyArea.
    let element: any = blocklyArea;
    let x = 0;
    var y = 0;
    do {
      x += element.offsetLeft;
      y += element.offsetTop;
      element = element.offsetParent!;
    } while (element);
    // Position blocklyDiv over blocklyArea.
    blocklyDiv.style.left = x + "px";
    blocklyDiv.style.top = y + "px";
    blocklyDiv.style.width = blocklyArea.offsetWidth + "px";
    blocklyDiv.style.height = blocklyArea.offsetHeight + "px";
  };
  window.addEventListener("resize", onresize, false);
  onresize();
}

const TYPES = [
  Blockly.Events.BLOCK_CREATE,
  Blockly.Events.BLOCK_DELETE,
  Blockly.Events.BLOCK_CHANGE,
  Blockly.Events.BLOCK_MOVE,
] as const;

export const BlocklyComponent = ({
  workspace,
  onWorkspaceChange,
  children,
  ...rest
}: BlocklyComponentProps) => {
  const ref = useRef<HTMLDivElement>(null);
  const toolbox = useRef();
  const onWorkspaceChangeRef = useRef(onWorkspaceChange);
  onWorkspaceChangeRef.current = onWorkspaceChange;

  useLayoutEffect(() => {
    Blockly.setLocale(locale);
    initBlocks();
    initGenerator();
    if (!ref.current) {
      return;
    }
    const blocklyWorkspace = Blockly.inject(ref.current, {
      toolbox: toolbox.current,
      media: "https://unpkg.com/blockly@12.5.1/media/",
      rendererOverrides: {
        ADD_START_HATS: true,
      },
      ...rest,
    });
    if (workspace) {
      Blockly.Xml.domToWorkspace(workspace, blocklyWorkspace);
    }

    const changeHandler = (event: any) => {
      if (TYPES.includes(event.type) && onWorkspaceChangeRef.current) {
        onWorkspaceChangeRef.current(event, blocklyWorkspace);
      }
    };

    if (onWorkspaceChangeRef.current) {
      blocklyWorkspace.addChangeListener(changeHandler);
    }
    resize();
    Blockly.svgResize(blocklyWorkspace);
    return () => blocklyWorkspace.removeChangeListener(changeHandler);
  }, []);

  return (
    <>
      <div id="blocklyArea" className={classes.blockly}>
        No blockly?
      </div>
      <div ref={ref} id="blocklyDiv" style={{ position: "absolute" }}></div>
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
