import React from "react";
import ListPresentation from "../shared/ListPresentation";

const Locations = (props) => {
  let filteredData = props.locations.map((loc) => {
    const container = {};
    container["city"] = loc.city;
    container["country"] = loc.country;
    return container;
  });

  return (
    <div className="container">
      <div>
        <h3 className="mt-4">Locations</h3>
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

export default Locations;
