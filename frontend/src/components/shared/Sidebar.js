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
                        <NavLink exact="true" to="/" activeclassname="activeClicked">
                            <CDBSidebarMenuItem icon="hippo">My lunches</CDBSidebarMenuItem>
                        </NavLink>
                        <NavLink exact="true" to="/whoisjoining" activeclassname="activeClicked">
                            <CDBSidebarMenuItem icon="user-friends">Who's joining?</CDBSidebarMenuItem>
                        </NavLink>
                        {props.isAdmin ? (
                            <div>
                                <NavLink exact="true" to="/allmenus" activeclassname="activeClicked">
                                    <CDBSidebarMenuItem icon="book">Menus</CDBSidebarMenuItem>
                                </NavLink>
                                <NavLink exact="true" to="/alldishes" activeclassname="activeClicked">
                                    <CDBSidebarMenuItem icon="utensils">Dishes</CDBSidebarMenuItem>
                                </NavLink>
                                <NavLink exact="true" to="/alloffices" activeclassname="activeClicked">
                                    <CDBSidebarMenuItem icon="map">Offices</CDBSidebarMenuItem>
                                </NavLink>
                                <NavLink exact="true" to="/allschedules" activeclassname="activeClicked">
                                    <CDBSidebarMenuItem icon="calendar">Schedules</CDBSidebarMenuItem>
                                </NavLink>
                                <NavLink exact="true" to="/monthlyreports" activeclassname="activeClicked">
                                    <CDBSidebarMenuItem icon="archive">Monthly Reports</CDBSidebarMenuItem>
                                </NavLink>

                            </div>
                        ) : (
                            <div></div>
                        )}

                        <NavLink exact="true" to="/userProfile" activeclassname="activeClicked">
                            <CDBSidebarMenuItem icon="user">Profile</CDBSidebarMenuItem>
                        </NavLink>
                        <NavLink exact="true" to="/" onClick={props.logout} activeclassname="activeClicked">
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