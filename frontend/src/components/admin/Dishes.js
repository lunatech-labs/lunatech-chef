import React from "react";
import ListPresentation from "../shared/ListPresentation";

const Dishes = (props) => {
  let filteredData = props.dishes.map((dish) => {
    const container = {};
    container["name"] = dish.name;
    container["description"] = dish.description;
    container["description"] = dish.description;
    container["isVegetarian"] = dish.isVegetarian;
    container["hasNuts"] = dish.hasNuts;
    container["hasSeafood"] = dish.hasSeafood;
    container["hasPork"] = dish.hasPork;
    container["hasBeef"] = dish.hasBeef;
    container["isGlutenFree"] = dish.isGlutenFree;
    container["hasLactose"] = dish.hasLactose;
    return container;
  });
  return (
    <div className="container">
      <div>
        <h3 className="mt-4">Dishes</h3>
      </div>
      <div className="col-12 col-md m-1">
        <ListPresentation
          isLoading={props.isLoading}
          error={props.error}
          data={filteredData}
        />
      </div>
    </div>
  );
};

export default Dishes;
