import React from "react";
import { Link, useNavigate } from "react-router-dom";
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import { Loading } from "../../shared/Loading";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMinus, faPlus, faEdit } from "@fortawesome/free-solid-svg-icons";

export default function ListLocations(props) {
  function ShowError({ error, reason }) {
    if (error) {
      return (
        <Row>
          <h4>
            An error ocurred when {reason} a location: {error}
          </h4>
        </Row>
      );
    } else {
      return <div></div>;
    }
  }

  function RenderData({ isLoading, error, locations }) {
    if (isLoading) {
      return (
        <Row>
          <Loading />
        </Row>
      );
    } else if (error) {
      return (
        <Row>
          <h4>An error ocurred when feching Locations from server: {error}</h4>
        </Row>
      );
    } else {
      return (
        <Row>
          <Table striped bordered hover>
            <thead>
              <tr>
                <th>City</th>
                <th>Country</th>
                <th></th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {locations.map((loc) => {
                return (
                  <tr key={loc.uuid}>
                    <td>{loc.city}</td>
                    <td>{loc.country}</td>
                    <td>
                      <Button
                        variant="primary"
                        value={loc.uuid}
                        onClick={() => handleEdit(loc)}
                      >
                        <FontAwesomeIcon icon={faEdit} />
                      </Button>
                    </td>
                    <td>
                      <Button
                        variant="danger"
                        value={loc.uuid}
                        onClick={() => handleRemove(loc.uuid)}
                      >
                        <FontAwesomeIcon icon={faMinus} />
                      </Button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </Table>
        </Row>
      );
    }
  }


  const handleRemove = (uuid) => {
    props.deleteLocation(uuid);
  }

  const navigate = useNavigate();
  const handleEdit = (location) => {
    navigate("/editlocation", { state: location });
  }

  return (
    <Container>
      <Row>
        <h3 className="mt-4">Management of Locations</h3>
      </Row>
      <Row>
        <Link to={`/newlocation`}>
          <button type="button" className="btn btn-success">
            <i>
              <FontAwesomeIcon icon={faPlus} />
            </i>{" "}
            New Location
          </button>
        </Link>
      </Row>
      <Row></Row>
      <RenderData
        isLoading={props.isLoading}
        error={props.errorListing}
        locations={props.locations}
      />
      <ShowError error={props.errorAdding} reason="adding" />
      <ShowError error={props.errorDeleting} reason="deleting" />
      <ShowError error={props.errorEditing} reason="saving" />
    </Container>
  );
}
