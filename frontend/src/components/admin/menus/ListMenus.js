import React, { Component } from "react";
import { Link } from "react-router-dom";
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import { Loading } from "../../shared/Loading";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMinus, faPlus, faEdit } from "@fortawesome/free-solid-svg-icons";

function ShowError({ error, reason }) {
  if (error) {
    return (
      <h4>
        An error ocurred when {reason} a menu: {error}
      </h4>
    );
  } else {
    return <div></div>;
  }
}

function RenderData({ isLoading, error, menus, handleEdit, handleRemove }) {
  if (isLoading) {
    return (
      <div className="container">
        <div className="row">
          <Loading />
        </div>
      </div>
    );
  } else if (error) {
    return (
      <div>
        <h4>An error ocurred when feching Menus from server: {error}</h4>
      </div>
    );
  } else {
    return (
      <div className="container">
        <div className="row">
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
        </div>
      </div>
    );
  }
}

class ListMenus extends Component {
  constructor(props) {
    super();
    this.handleRemove = this.handleRemove.bind(this);
    this.handleEdit = this.handleEdit.bind(this);
  }

  handleEdit(menu) {
    this.props.history.push("/editmenu", menu);
  }

  handleRemove(uuid) {
    this.props.deleteMenu(uuid);
  }

  render() {
    return (
      <div className="container">
        <div>
          <h3 className="mt-4">Management of Menus</h3>
        </div>
        <Link to={`/newMenu`}>
          <button type="button" className="btn btn-success">
            <i>
              <FontAwesomeIcon icon={faPlus} />
            </i>{" "}
            New Menu
          </button>
        </Link>
        <div>
          <RenderData
            isLoading={this.props.isLoading}
            error={this.props.errorListing}
            menus={this.props.menus}
            handleEdit={this.handleEdit}
            handleRemove={this.handleRemove}
          />
          <ShowError error={this.props.errorAdding} reason="adding" />
          <ShowError error={this.props.errorDeleting} reason="deleting" />
          <ShowError error={this.props.errorEditing} reason="saving" />
        </div>
      </div>
    );
  }
}

export default ListMenus;
