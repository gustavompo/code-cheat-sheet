//plotter https://codepen.io/gustavompo/pen/KvZbJw


function refine_interval(e,t,n){t&n?e[0]=(e[0]+e[1])/2:e[1]=(e[0]+e[1])/2}function calculateAdjacent(e,t){var n=(e=e.toLowerCase()).charAt(e.length-1),o=e.length%2?"odd":"even",d=e.substring(0,e.length-1);return-1!=BORDERS[t][o].indexOf(n)&&(d=calculateAdjacent(d,t)),d+BASE32[NEIGHBORS[t][o].indexOf(n)]}function decodeGeoHash(e){var t=1,n=[],o=[];for(n[0]=-90,n[1]=90,o[0]=-180,o[1]=180,lat_err=90,lon_err=180,i=0;i<e.length;i++)for(c=e[i],cd=BASE32.indexOf(c),j=0;j<5;j++)mask=BITS[j],t?(lon_err/=2,refine_interval(o,cd,mask)):(lat_err/=2,refine_interval(n,cd,mask)),t=!t;return n[2]=(n[0]+n[1])/2,o[2]=(o[0]+o[1])/2,{latitude:n,longitude:o}}function encodeGeoHash(e,t){var n=1,o=[],d=[],r=0,i=0;for(geohash="",o[0]=-90,o[1]=90,d[0]=-180,d[1]=180;geohash.length<12;)n?(mid=(d[0]+d[1])/2,t>mid?(i|=BITS[r],d[0]=mid):d[1]=mid):(mid=(o[0]+o[1])/2,e>mid?(i|=BITS[r],o[0]=mid):o[1]=mid),n=!n,r<4?r++:(geohash+=BASE32[i],r=0,i=0);return geohash}BITS=[16,8,4,2,1],BASE32="0123456789bcdefghjkmnpqrstuvwxyz",NEIGHBORS={right:{even:"bc01fg45238967deuvhjyznpkmstqrwx"},left:{even:"238967debc01fg45kmstqrwxuvhjyznp"},top:{even:"p0r21436x8zb9dcf5h7kjnmqesgutwvy"},bottom:{even:"14365h7k9dcfesgujnmqp0r2twvyx8zb"}},BORDERS={right:{even:"bcfguvyz"},left:{even:"0145hjnp"},top:{even:"prxz"},bottom:{even:"028b"}},NEIGHBORS.bottom.odd=NEIGHBORS.left.even,NEIGHBORS.top.odd=NEIGHBORS.right.even,NEIGHBORS.left.odd=NEIGHBORS.bottom.even,NEIGHBORS.right.odd=NEIGHBORS.top.even,BORDERS.bottom.odd=BORDERS.left.even,BORDERS.top.odd=BORDERS.right.even,BORDERS.left.odd=BORDERS.bottom.even,BORDERS.right.odd=BORDERS.top.even;



function initialize() {
  // Map Center
  var myLatLng = new google.maps.LatLng(-23.562174, -46.633037);
  // General Options
  var mapOptions = {
    zoom: 12,
    center: myLatLng,
    mapTypeId: google.maps.MapTypeId.RoadMap
  };
  var map = new google.maps.Map(document.getElementById('map-canvas'),mapOptions);
  points.map(p => new google.maps.Circle({
            strokeColor: '#0000FF',
            strokeOpacity: 0.8,
            strokeWeight: 1,
            fillColor: '#0000FF',
            fillOpacity: 0.35,
            map: map,
            center: p,
            radius: 15
          }));

//   geohashesv2.map(g =>{
//   let ghObj = decodeGeoHash(g)
//   let latlngs = [new google.maps.LatLng(ghObj.latitude[0], ghObj.longitude[0]),
//                    new google.maps.LatLng(ghObj.latitude[0], ghObj.longitude[1]),
//   new google.maps.LatLng(ghObj.latitude[1], ghObj.longitude[1]),

//   new google.maps.LatLng(ghObj.latitude[1], ghObj.longitude[0]), new google.maps.LatLng(ghObj.latitude[0], ghObj.longitude[0])]

//   let myPolygon = new google.maps.Polygon({
//     paths: latlngs,
//     draggable: false, // turn off if it gets annoying
//     editable: false,
//     strokeColor: '#0000FF',
//     strokeOpacity: 0.5,
//     strokeWeight: 1,
//     fillColor: '#0000FF',
//     fillOpacity: 0.35
//   });
//   myPolygon.setMap(map);

// })
  
  geohashes.map(g =>{
  let ghObj = decodeGeoHash(g)
  let latlngs = [new google.maps.LatLng(ghObj.latitude[0], ghObj.longitude[0]),
                   new google.maps.LatLng(ghObj.latitude[0], ghObj.longitude[1]),
  new google.maps.LatLng(ghObj.latitude[1], ghObj.longitude[1]),

  new google.maps.LatLng(ghObj.latitude[1], ghObj.longitude[0]), new google.maps.LatLng(ghObj.latitude[0], ghObj.longitude[0])]

  let myPolygon = new google.maps.Polygon({
    paths: latlngs,
    draggable: false, // turn off if it gets annoying
    editable: false,
    strokeColor: '#FF0000',
    strokeOpacity: 0.8,
    strokeWeight: 1,
    fillColor: '#FF0000',
    fillOpacity: 0.30
  });
  myPolygon.setMap(map);

})
}
