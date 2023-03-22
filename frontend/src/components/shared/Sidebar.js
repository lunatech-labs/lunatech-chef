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
import {MenuItems} from "../../content/menuInfo";
import { useLocation } from 'react-router-dom'

// based on https://medium.com/@devwares/how-to-create-a-responsive-sidebar-in-react-using-bootstrap-and-contrast-86d0829f8c6c


const Sidebar = (props) => {
    const location = useLocation();

    const isActive = (path) => {
        const splittedPath = path.split("/")[1];
        const splittedPathname = location.pathname.split("/")[1];
        return splittedPath === splittedPathname;
    }

    const RenderNavLink = ()=>{
        const adminLinks = MenuItems.filter(item=> item.isAdmin === props.isAdmin);
        const nonAdminLinks = MenuItems.filter(item=> item.isAdmin === false);
        const fullLinks = props.isAdmin ? [...nonAdminLinks, ...adminLinks] : nonAdminLinks;
        const sortedLinks = fullLinks.sort((a,b)=> a.id - b.id);
        return  sortedLinks.map((item, index)=>{
            return (item.name === "Logout"?
                   <NavLink
                       key={index}
                       exact="true"
                       to={item.link}
                       onClick={props.logout}>
                    <CDBSidebarMenuItem active={isActive(item.link)} icon={item.icon}>{item.name}</CDBSidebarMenuItem>
                </NavLink>
                    :
                 <NavLink
                      key={index}
                      to={item.link}>
                    <CDBSidebarMenuItem active={isActive(item.link)} icon={item.icon}>{item.name}</CDBSidebarMenuItem>
                </NavLink>
            )
        })
    }
    return (
        <div className="sidebar">
            <CDBSidebar textColor="#000" backgroundColor="#f0f0f0">
                <CDBSidebarHeader prefix={<i className="fa fa-bars fa-large"></i>}>Lunatech Chef</CDBSidebarHeader>
                <CDBSidebarContent className="sidebar-content">
                    <CDBSidebarMenu>
                       <RenderNavLink />
                    </CDBSidebarMenu>
                </CDBSidebarContent>
                <CDBSidebarFooter>
                    {process.env.NODE_ENV === "production" ? (
                        <img src={process.env.PUBLIC_URL + '/lunatech-logo.png'} alt="Lunatech logo" width="270px" />)
                        : (
                            <img src={process.env.PUBLIC_URL + '/lunatech-logo.png'} alt="Lunatech logo" width="270px" />)
                    }
                </CDBSidebarFooter>
            </CDBSidebar>
        </div>
    );
};

export default Sidebar;