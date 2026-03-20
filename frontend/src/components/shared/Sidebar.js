import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
    faBars,
    faHippo,
    faUserFriends,
    faBook,
    faUtensils,
    faMap,
    faCalendar,
    faArchive,
    faUser,
    faUserSlash,
} from '@fortawesome/free-solid-svg-icons';

const Sidebar = (props) => {
    const [collapsed, setCollapsed] = useState(false);

    return (
        <div className="sidebar">
            <nav className={`sidebar-panel${collapsed ? ' sidebar-collapsed' : ''}`}>
                <div className="sidebar-header">
                    <button className="sidebar-toggle" onClick={() => setCollapsed(c => !c)}>
                        <FontAwesomeIcon icon={faBars} />
                    </button>
                    {!collapsed && <span className="sidebar-title">Lunatech's Chef</span>}
                </div>
                <div className="sidebar-content">
                    <ul className="sidebar-menu">
                        <li>
                            <NavLink to="/" className={({ isActive }) => isActive ? "sidebar-link activeClicked" : "sidebar-link"}>
                                <FontAwesomeIcon icon={faHippo} fixedWidth />
                                {!collapsed && <span>My lunches</span>}
                            </NavLink>
                        </li>
                        <li>
                            <NavLink to="/whoisjoining" className={({ isActive }) => isActive ? "sidebar-link activeClicked" : "sidebar-link"}>
                                <FontAwesomeIcon icon={faUserFriends} fixedWidth />
                                {!collapsed && <span>Who's joining?</span>}
                            </NavLink>
                        </li>
                        {props.isAdmin && (
                            <>
                                <li>
                                    <NavLink to="/allmenus" className={({ isActive }) => isActive ? "sidebar-link activeClicked" : "sidebar-link"}>
                                        <FontAwesomeIcon icon={faBook} fixedWidth />
                                        {!collapsed && <span>Menus</span>}
                                    </NavLink>
                                </li>
                                <li>
                                    <NavLink to="/alldishes" className={({ isActive }) => isActive ? "sidebar-link activeClicked" : "sidebar-link"}>
                                        <FontAwesomeIcon icon={faUtensils} fixedWidth />
                                        {!collapsed && <span>Dishes</span>}
                                    </NavLink>
                                </li>
                                <li>
                                    <NavLink to="/alloffices" className={({ isActive }) => isActive ? "sidebar-link activeClicked" : "sidebar-link"}>
                                        <FontAwesomeIcon icon={faMap} fixedWidth />
                                        {!collapsed && <span>Offices</span>}
                                    </NavLink>
                                </li>
                                <li>
                                    <NavLink to="/allschedules" className={({ isActive }) => isActive ? "sidebar-link activeClicked" : "sidebar-link"}>
                                        <FontAwesomeIcon icon={faCalendar} fixedWidth />
                                        {!collapsed && <span>Schedules</span>}
                                    </NavLink>
                                </li>
                                <li>
                                    <NavLink to="/monthlyreports" className={({ isActive }) => isActive ? "sidebar-link activeClicked" : "sidebar-link"}>
                                        <FontAwesomeIcon icon={faArchive} fixedWidth />
                                        {!collapsed && <span>Monthly Reports</span>}
                                    </NavLink>
                                </li>
                            </>
                        )}
                        <li>
                            <NavLink to="/userProfile" className={({ isActive }) => isActive ? "sidebar-link activeClicked" : "sidebar-link"}>
                                <FontAwesomeIcon icon={faUser} fixedWidth />
                                {!collapsed && <span>Profile</span>}
                            </NavLink>
                        </li>
                        <li>
                            <NavLink to="/" onClick={props.logout} className="sidebar-link">
                                <FontAwesomeIcon icon={faUserSlash} fixedWidth />
                                {!collapsed && <span>Logout</span>}
                            </NavLink>
                        </li>
                    </ul>
                </div>
                {!collapsed && (
                    <div className="sidebar-footer">
                        <img src="/lunatech-logo.png" alt="Lunatech logo" width="270px" />
                    </div>
                )}
            </nav>
        </div>
    );
};

export default Sidebar;
