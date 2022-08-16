import { HTMLAttributes, useState } from "react";
// @ts-ignore
import classes from "./toolbar.module.css";

type ToolbarProps = HTMLAttributes<HTMLElement> & {
  onTabSwitch: (activeTab: number) => void;
};

export const Toolbar = ({ onTabSwitch, ...rest }: ToolbarProps) => {
  const [tab, setTab] = useState(1);

  const switchTab = (tab: number) => {
    setTab(tab);
    onTabSwitch(tab);
  };

  return (
    <nav {...rest}>
      <img
        className={classes.logo}
        src="logo.png"
      />
      <div className={classes.tab}>
        <div
          id={classes.bl}
          className={tab === 1 ? classes.active : ""}
          onClick={() => switchTab(1)}
        >
          <i className="fa-solid fa-puzzle-piece"></i>Blocks
        </div>
        <div
          id={classes.js}
          className={tab === 2 ? classes.active : ""}
          onClick={() => switchTab(2)}
        >
          <i className="fa-brands fa-js"></i>Javascript
        </div>
        <span className={classes.glider}></span>
      </div>
    </nav>
  );
};
