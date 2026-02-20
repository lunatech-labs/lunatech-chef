import React from 'react';
import {
    CDBSidebar,
    CDBSidebarContent,
    CDBSidebarFooter,
    CDBSidebarHeader,
    CDBSidebarMenu,
    CDBSidebarMenuItem,
} from 'cdbreact';
import { NavLink } from 'react-router-dom';

// based on https://medium.com/@devwares/how-to-create-a-responsive-sidebar-in-react-using-bootstrap-and-contrast-86d0829f8c6c
const Sidebar = (props) => {
    return (
        <div className="sidebar">
            <CDBSidebar textColor="#000" backgroundColor="#f0f0f0">
                <CDBSidebarHeader prefix={<i className="fa fa-bars fa-large"></i>}>Lunatech's Chef</CDBSidebarHeader>
                <CDBSidebarContent className="sidebar-content">
                    <CDBSidebarMenu>
                        <NavLink to="/" className={({ isActive }) => isActive ? "activeClicked" : ""}>
                            <CDBSidebarMenuItem icon="hippo">My lunches</CDBSidebarMenuItem>
                        </NavLink>
                        <NavLink to="/whoisjoining" className={({ isActive }) => isActive ? "activeClicked" : ""}>
                            <CDBSidebarMenuItem icon="user-friends">Who's joining?</CDBSidebarMenuItem>
                        </NavLink>
                        {props.isAdmin ? (
                            <div>
                                <NavLink to="/allmenus" className={({ isActive }) => isActive ? "activeClicked" : ""}>
                                    <CDBSidebarMenuItem icon="book">Menus</CDBSidebarMenuItem>
                                </NavLink>
                                <NavLink to="/alldishes" className={({ isActive }) => isActive ? "activeClicked" : ""}>
                                    <CDBSidebarMenuItem icon="utensils">Dishes</CDBSidebarMenuItem>
                                </NavLink>
                                <NavLink to="/alloffices" className={({ isActive }) => isActive ? "activeClicked" : ""}>
                                    <CDBSidebarMenuItem icon="map">Offices</CDBSidebarMenuItem>
                                </NavLink>
                                <NavLink to="/allschedules" className={({ isActive }) => isActive ? "activeClicked" : ""}>
                                    <CDBSidebarMenuItem icon="calendar">Schedules</CDBSidebarMenuItem>
                                </NavLink>
                                <NavLink to="/monthlyreports" className={({ isActive }) => isActive ? "activeClicked" : ""}>
                                    <CDBSidebarMenuItem icon="archive">Monthly Reports</CDBSidebarMenuItem>
                                </NavLink>
                            </div>
                        ) : null}
                        <NavLink to="/userProfile" className={({ isActive }) => isActive ? "activeClicked" : ""}>
                            <CDBSidebarMenuItem icon="user">Profile</CDBSidebarMenuItem>
                        </NavLink>
                        <NavLink to="/" onClick={props.logout} className="">
                            <CDBSidebarMenuItem icon="user-slash">Logout</CDBSidebarMenuItem>
                        </NavLink>
                    </CDBSidebarMenu>
                </CDBSidebarContent>
                <CDBSidebarFooter>
                    <img src={process.env.PUBLIC_URL + 'lunatech-logo.png'} alt="Lunatech logo" width="270px" />
                </CDBSidebarFooter>
            </CDBSidebar>
        </div>
    );
};

export default Sidebar;
