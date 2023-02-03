import React from "react";
import { Link, useNavigate } from "react-router-dom";
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import { Loading } from "../../shared/Loading";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMinus, faPlus, faEdit } from "@fortawesome/free-solid-svg-icons";

export default function ListMenus(props) {
  function ShowError({ error, reason }) {
    if (error) {
      return (
        <Row>
          An error ocurred when {reason} a menu: {error}
        </Row>
      );
    } else {
      return <div></div>;
    }
  }

  function RenderData({ isLoading, error, menus }) {
    if (isLoading) {
      return (
        <Row>
          <Loading />
        </Row>
      );
    } else if (error) {
      return (
        <Row>
          <h4>An error ocurred when feching Menus from server: {error}</h4>
        </Row>
      );
    } else {
      return (
        <Row>
          <Table striped bordered hover>
            <thead>
              <tr>
                <th>Name</th>
                <th>Dishes</th>
                <th></th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {menus.map((menu) => {
                return (
                  <tr key={menu.uuid}>
                    <td>{menu.name}</td>
                    <td>
                      {menu.dishes.map((dish) => (
                        <p key={dish.uuid}>{dish.name}</p>
                      ))}
                    </td>
                    <td>
                      <Button
                        variant="primary"
                        value={menu.uuid}
                        onClick={() => handleEdit(menu)}
                      >
                        <FontAwesomeIcon icon={faEdit} />
                      </Button>
                    </td>
                    <td>
                      <Button
                        variant="danger"
                        value={menu.uuid}
                        onClick={() => handleRemove(menu.uuid)}
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

  const navigate = useNavigate();
  const handleEdit = (menu) => {
    navigate("/editmenu", { state: menu });
  }


  const handleRemove = (uuid) => {
    props.deleteMenu(uuid);
  }

  return (
    <Container>
      <Row>
        <h3 className="mt-4">Management of Menus</h3>
      </Row>
      <Row>
        <Link to="/newMenu">
          <button type="button" className="btn btn-success">
            <i>
              <FontAwesomeIcon icon={faPlus} />
            </i>{" "}
            New Menu
          </button>
        </Link>
      </Row>
      <Row></Row>
      <RenderData
        isLoading={props.isLoading}
        error={props.errorListing}
        menus={props.menus}
      />
      <ShowError error={props.errorAdding} reason="adding" />
      <ShowError error={props.errorDeleting} reason="deleting" />
      <ShowError error={props.errorEditing} reason="saving" />
    </Container>
  );
}

