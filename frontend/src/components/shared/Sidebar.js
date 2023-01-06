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

const Sidebar = (props) => {
    return (
        <div className="sidebar">
            <CDBSidebar textColor="#000" backgroundColor="#f0f0f0">
                <CDBSidebarHeader prefix={<i className="fa fa-bars fa-large"></i>}> Lunatech Chef</CDBSidebarHeader>
                <CDBSidebarContent className="sidebar-content">
                    <CDBSidebarMenu>
                        <NavLink exact="true" to="/" activeclassname="activeClicked">
                            <CDBSidebarMenuItem icon="hippo">Meals schedules</CDBSidebarMenuItem>
                        </NavLink>
                        <NavLink exact="true" to="/whoisjoining" activeclassname="activeClicked">
                            <CDBSidebarMenuItem icon="question">Who is joining?</CDBSidebarMenuItem>
                        </NavLink>
                        {props.isAdmin ? (
                            <div>
                                <NavLink exact="true" to="/alllocations" activeclassname="activeClicked">
                                    <CDBSidebarMenuItem icon="map">Locations</CDBSidebarMenuItem>
                                </NavLink>
                                <NavLink exact="true" to="/alldishes" activeclassname="activeClicked">
                                    <CDBSidebarMenuItem icon="utensils">Dishes</CDBSidebarMenuItem>
                                </NavLink>
                                <NavLink exact="true" to="/allmenus" activeclassname="activeClicked">
                                    <CDBSidebarMenuItem icon="folder">Menus</CDBSidebarMenuItem>
                                </NavLink>
                                <NavLink exact="true" to="/allschedules" activeclassname="activeClicked">
                                    <CDBSidebarMenuItem icon="calendar">Schedules</CDBSidebarMenuItem>
                                </NavLink>
                            </div>
                        ) : (
                            <div></div>
                        )}

                        <NavLink exact="true" to="/userProfile" activeclassname="activeClicked">
                            <CDBSidebarMenuItem icon="user">Profile</CDBSidebarMenuItem>
                        </NavLink>
                        <NavLink exact="true" onClick={props.logout} activeclassname="activeClicked">
                            <CDBSidebarMenuItem icon="user-slash">Logout</CDBSidebarMenuItem>
                        </NavLink>
                    </CDBSidebarMenu>
                </CDBSidebarContent>
                <CDBSidebarFooter>
                    {process.env.NODE_ENV === "production" ? (
                        <img src={process.env.PUBLIC_URL + 'root/lunatech-logo.png'} alt="Lunatech logo" width="270px" />)
                        : (
                            <img src={process.env.PUBLIC_URL + 'lunatech-logo.png'} alt="Lunatech logo" width="270px" />)
                    }
                </CDBSidebarFooter>
            </CDBSidebar>
        </div>
    );
};

export default Sidebar;