import { ReactNode } from "react";

type BlockProps = { [x: string]: any; children?: ReactNode };

export const Block = (p: BlockProps) => {
  const { children, ...props } = p;
  return (
    <block is="blockly" {...props}>
      {children}
    </block>
  );
};

export const Category = (p: BlockProps) => {
  const { children, ...props } = p;
  return (
    <category is="blockly" {...props}>
      {children}
    </category>
  );
};

export const Value = (p: BlockProps) => {
  const { children, ...props } = p;
  return (
    <value is="blockly" {...props}>
      {children}
    </value>
  );
};

export const Field = (p: BlockProps) => {
  const { children, ...props } = p;
  return (
    <field is="blockly" {...props}>
      {children}
    </field>
  );
};

export const Shadow = (p: BlockProps) => {
  const { children, ...props } = p;
  return (
    <shadow is="blockly" {...props}>
      {children}
    </shadow>
  );
};
