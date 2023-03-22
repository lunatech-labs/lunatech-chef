
import React, {useEffect, useState} from 'react';
import {useParams, useNavigate, Outlet, useLocation, Navigate} from 'react-router-dom';
import Row from "react-bootstrap/Row";
import Container from "react-bootstrap/Container";
import Tab from 'react-bootstrap/Tab';
import Tabs from 'react-bootstrap/Tabs';
import {UserProfile} from "./UserProfile";
import AddToken from "./admin/developer/AddToken";

import ProtectedRoutes from "./auth/ProtectedRoutes";

export default function Settings(props) {
 const { id } = useParams();

 const validIds  = ['1', '2']
 const [key, setKey] = useState(id??1);
 const navigate = useNavigate();
 const useLocation1 = useLocation();

    useEffect(() => {
        // clear token on page load
        if(useLocation1.pathname !== '/settings/2' && props.user.token){
            props.deleteToken()
        }
    }, [props.user.token])

 const handleSelect = (key) => {
     setKey(key)
     navigate(`/settings/${key}`)
 }

    if (!validIds.includes(id)) {
        navigate('/')
    }

    const Profile = () => {
        return (
            <UserProfile
                user={props.user}
                locations={props.locations}
                saveUserProfile={props.saveUserProfile}
            />
        );
    };

    const API = () => {
        return (
            <AddToken user={props.user} handleTokenGeneration = {props.generateToken} deleteToken={props.deleteToken}/>
        )
    }
    const RenderData = () => {
     return (
         <Tabs
             id="controlled-tab-settings"
             activeKey={key}
             onSelect={(k) => handleSelect(k)}
             className="mb-3"
             fill
         >
             <Tab eventKey="1" title="User Profile">
                <Profile />
             </Tab>
             <Tab eventKey="2" title="API" disabled={!props.user.isAdmin}>
                 {
                     props.user.isAdmin && <API />
                 }
             </Tab>
         </Tabs>
     )
    }

    return (
        <Container>
         <Row>
          <h3 className="my-4">Settings</h3>
         </Row>
         <RenderData />
        </Container>
    )



}