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
        <div className="sidebar" >
            <CDBSidebar textColor="#fff" backgroundColor="#333">
                <CDBSidebarHeader prefix={<i className="fa fa-bars fa-large"></i>}> Lunatech Chef </CDBSidebarHeader>
                <CDBSidebarContent className="sidebar-content">
                    <CDBSidebarMenu>
                        <NavLink exact to="/" activeClassName="activeClicked">
                            <CDBSidebarMenuItem icon="hippo">Meals schedules</CDBSidebarMenuItem>
                        </NavLink>
                        <NavLink exact to="/whoisjoining" activeClassName="activeClicked">
                            <CDBSidebarMenuItem icon="question">Who is joining?</CDBSidebarMenuItem>
                        </NavLink>
                        {props.isAdmin ? (
                            <div>
                                <NavLink exact to="/alllocations" activeClassName="activeClicked">
                                    <CDBSidebarMenuItem icon="map">Locations</CDBSidebarMenuItem>
                                </NavLink>
                                <NavLink exact to="/alldishes" activeClassName="activeClicked">
                                    <CDBSidebarMenuItem icon="utensils">Dishes</CDBSidebarMenuItem>
                                </NavLink>
                                <NavLink exact to="/allmenus" activeClassName="activeClicked">
                                    <CDBSidebarMenuItem icon="folder">Menus</CDBSidebarMenuItem>
                                </NavLink>
                                <NavLink exact to="/allschedules" activeClassName="activeClicked">
                                    <CDBSidebarMenuItem icon="calendar">Schedules</CDBSidebarMenuItem>
                                </NavLink>
                            </div>
                        ) : (
                            <div></div>
                        )}

                        <NavLink exact to="/userProfile" activeClassName="activeClicked">
                            <CDBSidebarMenuItem icon="user">Profile</CDBSidebarMenuItem>
                        </NavLink>
                        <NavLink exact onClick={props.logout} activeClassName="activeClicked">
                            <CDBSidebarMenuItem icon="user-slash">Logout</CDBSidebarMenuItem>
                        </NavLink>
                    </CDBSidebarMenu>
                </CDBSidebarContent>
                <CDBSidebarFooter>
                    <img src={process.env.PUBLIC_URL + '/lunatech-logo.png'} alt="Lunatech logo" width="270px" />
                </CDBSidebarFooter>
            </CDBSidebar>
        </div>
    );
};

export default Sidebar;