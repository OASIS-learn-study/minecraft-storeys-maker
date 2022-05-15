// JavaScript code generators see "./blocks.ts"

import Blockly from "blockly/core";
import BlocklyJS from "blockly/javascript";
import "blockly/javascript";
import { Block } from "core/blockly";

export default function initGenerator() {
  Blockly.JavaScript["when_event"] = (block: Block) => {
    const whenStatements = Blockly.JavaScript.statementToCode(block, "THEN");
    return (
      'whenEvent("' +
      block.getField("EVENT")?.getValue() +
      '", () => {\n' +
      whenStatements +
      "\n});\n"
    );
  };

  Blockly.JavaScript["when_right_clicked"] = (block: Block) => {
    const whenStatements = Blockly.JavaScript.statementToCode(block, "THEN");
    const entity = block.getField("ENTITY")?.getValue();
    return (
      'whenRightClicked("' +
      entity +
      '", () => {\n' +
      whenStatements +
      "\n});\n"
    );
  };

  Blockly.JavaScript["when_command"] = (block: Block) => {
    const whenStatements = Blockly.JavaScript.statementToCode(block, "THEN");
    const command = block.getField("COMMAND")?.getValue();
    return (
      'whenCommand("' + command + '", () => {\n' + whenStatements + "\n});\n"
    );
  };

  Blockly.JavaScript["when_inside"] = (block: Block) => {
    const whenStatements = Blockly.JavaScript.statementToCode(block, "THEN");
    const areaName = block.getField("AREA")?.getValue();
    return (
      'whenInside("' + areaName + '", () => {\n' + whenStatements + "\n});\n"
    );
  };

  Blockly.JavaScript["narrate"] = (block: Block) => {
    const name = block.getField("ENTITY")?.getValue();
    const text = Blockly.JavaScript.valueToCode(
      block,
      "TEXT",
      Blockly.JavaScript.ORDER_ATOMIC
    );

    return 'minecraft.say("' + name + '", ' + text + ");\n";
  };

  Blockly.JavaScript["minecraftCommand"] = (block: Block) => {
    const command = Blockly.JavaScript.valueToCode(
      block,
      "COMMAND",
      Blockly.JavaScript.ORDER_ATOMIC
    );
    return 'minecraft.cmd(' + command + ');\n';
  };

  Blockly.JavaScript["addRemoveItem"] = (block: Block) => {
    const amount = Blockly.JavaScript.valueToCode(
      block,
      "AMOUNT",
      Blockly.JavaScript.ORDER_ATOMIC
    );
    const item = Blockly.JavaScript.valueToCode(
      block,
      "ITEM",
      Blockly.JavaScript.ORDER_ATOMIC
    );
    return "minecraft.addRemoveItem(" + amount + ", " + item + ");\n";
  };

  Blockly.JavaScript["showTitle"] = (block: Block) => {
    const text = Blockly.JavaScript.valueToCode(
      block,
      "TEXT",
      Blockly.JavaScript.ORDER_ATOMIC
    );

    return "minecraft.showTitle(" + text + ");\n";
  };

  Blockly.JavaScript["items"] = (block: Block) => [
    block.getField("ITEM")?.getValue(),
    Blockly.JavaScript.ORDER_ATOMIC,
  ];

  Blockly.JavaScript["itemHeld"] = (block: Block) => [
    "minecraft.player().getItemInHand()",
    Blockly.JavaScript.ORDER_ATOMIC,
  ];

  Blockly.JavaScript["lastPlayerJoined"] = (block: Block) => [
    "Erik",
    Blockly.JavaScript.ORDER_ATOMIC,
  ];
}

export const generate = (workspace: any) =>
  BlocklyJS.workspaceToCode(workspace);
