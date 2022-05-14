import Blockly from "blockly/core";

const EVENT_BLOCK = [
  {
    type: "when_event",
    message0: "When %1 %2 %3",
    args0: [
      {
        type: "field_dropdown",
        name: "EVENT",
        options: [["Player Joined", "playerJoined"]],
      },
      {
        type: "input_dummy",
      },
      {
        type: "input_statement",
        name: "THEN",
      },
    ],
    "colour": 160,
  },
  {
    type: "when_right_clicked",
    message0: "When %1 right clicked %2 %3",
    args0: [
      {
        type: "field_input",
        name: "ENTITY",
        text: "entity",
        check: "String",
      },
      {
        type: "input_dummy",
      },
      {
        type: "input_statement",
        name: "THEN",
      },
    ],
    "colour": 160,
  },
  {
    type: "when_command",
    message0: "When /%1 %2 %3",
    args0: [
      {
        type: "field_input",
        name: "COMMAND",
        text: "demo",
        check: "String",
      },
      {
        type: "input_dummy",
      },
      {
        type: "input_statement",
        name: "THEN",
      },
    ],
    "colour": 160,
  },

  {
    type: "when_inside",
    message0: "When inside %1 %2 %3",
    args0: [
      {
        type: "field_input",
        name: "AREA",
        text: "name",
        check: "String",
      },
      {
        type: "input_dummy",
      },
      {
        type: "input_statement",
        name: "THEN",
      },
    ],
    "colour": 160,
  },
  {
    type: "narrate",
    message0: "%1 speaks %2",
    args0: [
      {
        type: "field_input",
        name: "ENTITY",
        text: "entity",
        check: "String",
      },
      {
        type: "input_value",
        name: "TEXT",
        check: "String",
      },
    ],
    colour: 45,
    previousStatement: null,
    nextStatement: null,
  },
  {
    type: "minecraftCommand",
    message0: "/%1",
    args0: [
      {
        type: "field_input",
        name: "COMMAND",
        text: "demo",
        check: "String",
      },
    ],
    colour: 45,
    previousStatement: null,
    nextStatement: null,
  },
  {
    type: "addRemoveItem",
    message0: "give / take %1 %2",
    args0: [
      {
        type: "input_value",
        name: "AMOUNT",
        check: "Number",
      },
      {
        type: "input_value",
        name: "ITEM",
        check: "item",
      },
    ],
    inputsInline: true,
    colour: 45,
    previousStatement: null,
    nextStatement: null,
  },
  {
    type: "showTitle",
    message0: "title %1",
    args0: [
      {
        type: "input_value",
        name: "TEXT",
        check: "String",
      },
    ],
    colour: 45,
    previousStatement: null,
    nextStatement: null,
  },
  {
    type: "items",
    message0: "%1",
    args0: [
      {
        type: "field_dropdown",
        name: "ITEM",
        options: [
          ["Nothing", "ItemTypes.Nothing"],
          ["Apple", "ItemTypes.Apple"],
          ["Beef", "ItemTypes.Beef"],
          ["Beetroot", "ItemTypes.Beetroot"],
          ["Boat", "ItemTypes.Boat"],
          ["Book", "ItemTypes.Book"],
          ["Bow", "ItemTypes.Bow"],
          ["Bowl", "ItemTypes.Bowl"],
          ["Bread", "ItemTypes.Bread"],
          ["Cactus", "ItemTypes.Cactus"],
          ["Cake", "ItemTypes.Cake"],
          ["Carrot", "ItemTypes.Carrot"],
          ["Cauldron", "ItemTypes.Cauldron"],
          ["Chicken", "ItemTypes.Chicken"],
          ["Clock", "ItemTypes.Clock"],
          ["Cookie", "ItemTypes.Cookie"],
        ],
      },
    ],
    output: "item",
    colour: 210,
  },
  {
    type: "itemHeld",
    message0: "Item held",
    output: "item",
    colour: 210,
  },

  {
    type: "lastPlayerJoined",
    message0: "Last player joined",
    output: "String",
    colour: 210,
  },
] as const;

export default function initBlocks() {
  EVENT_BLOCK.map(
    (b) =>
      (Blockly.Blocks[b.type] = {
        init: function () {
          this.jsonInit(b);
        },
      })
  );
}
