import { useState } from "react";
// @ts-ignore
import classes from "./toolbar.module.css";

type ToolbarProps = {
  className: string;
  onTabSwitch: (activeTab: number) => void;
};

export const Toolbar = ({ onTabSwitch, ...rest }: ToolbarProps) => {
  const [tab, setTab] = useState(1);

  const switchTab = (tab: number) => {
    setTab(tab);
    onTabSwitch(tab);
  }

  return (
    <nav {...rest}>
      <div className={classes.tab}>
        <img src="https://raw.githubusercontent.com/teneresa/minecraft-storeys-maker/master/logo%20design/Logo%20OASIS%20small.png" />
        <button className={tab === 1 ? classes.active : ""} onClick={() => switchTab(1)}>
          <i className="fa-solid fa-puzzle-piece"></i>Blocks
        </button>
        <button className={tab === 2 ? classes.active : ""} onClick={() => switchTab(2)}>
          <i className="fa-brands fa-js"></i>Javascript
        </button>
      </div>
    </nav>
  );
};
