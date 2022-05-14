// JavaScript code generators see "./blocks.ts"

import Blockly from "blockly/core";
import BlocklyJS from "blockly/javascript";
import "blockly/javascript";

type BlockType = {
  getField: (arg0: string) => {
    (): any;
    new (): any;
    getValue: { (): string; new (): any };
  };
};

export default function initGenerator() {
  Blockly.JavaScript["when_event"] = (block: BlockType) => {
    const whenStatements = Blockly.JavaScript.statementToCode(block, "THEN");
    return (
      'whenEvent("' +
      block.getField("EVENT").getValue() +
      '", () => {\n' +
      whenStatements +
      "\n});\n"
    );
  };

  Blockly.JavaScript["when_right_clicked"] = (block: BlockType) => {
    const whenStatements = Blockly.JavaScript.statementToCode(block, "THEN");
    return (
      'whenRightClicked("' +
      block.getField("ENTITY").getValue() +
      '", () => {\n' +
      whenStatements +
      "\n});\n"
    );
  };

  Blockly.JavaScript["when_command"] = (block: BlockType) => {
    const whenStatements = Blockly.JavaScript.statementToCode(block, "THEN");
    return (
      'whenCommand("' +
      block.getField("COMMAND").getValue() +
      '", () => {\n' +
      whenStatements +
      "\n});\n"
    );
  };

  Blockly.JavaScript["when_inside"] = (block: BlockType) => {
    const whenStatements = Blockly.JavaScript.statementToCode(block, "THEN");
    return (
      'whenInside("' +
      block.getField("AREA").getValue() +
      '", () => {\n' +
      whenStatements +
      "\n});\n"
    );
  };

  Blockly.JavaScript["narrate"] = (block: BlockType) => {
    const name = block.getField("ENTITY").getValue();
    const text = Blockly.JavaScript.valueToCode(
      block,
      "TEXT",
      Blockly.JavaScript.ORDER_ATOMIC
    );

    return 'minecraft.say("' + name + '", ' + text + ");\n";
  };

  Blockly.JavaScript["minecraftCommand"] = (block: BlockType) => {
    return 'minecraft.cmd("' + block.getField("COMMAND").getValue() + '");\n';
  };

  Blockly.JavaScript["addRemoveItem"] = (block: BlockType) => {
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

  Blockly.JavaScript["showTitle"] = (block: BlockType) => {
    const text = Blockly.JavaScript.valueToCode(
      block,
      "TEXT",
      Blockly.JavaScript.ORDER_ATOMIC
    );

    return "minecraft.showTitle(" + text + ");\n";
  };

  Blockly.JavaScript["items"] = (block: BlockType) => [
    block.getField("ITEM").getValue(),
    Blockly.JavaScript.ORDER_ATOMIC,
  ];

  Blockly.JavaScript["itemHeld"] = (block: BlockType) => [
    "minecraft.player().getItemInHand()",
    Blockly.JavaScript.ORDER_ATOMIC,
  ];

  Blockly.JavaScript["lastPlayerJoined"] = (block: BlockType) => [
    "Erik",
    Blockly.JavaScript.ORDER_ATOMIC,
  ];
}

export const generate = (workspace: any) =>
  BlocklyJS.workspaceToCode(workspace);
