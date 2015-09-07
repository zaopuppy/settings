# design

## interface

[ ] register
[ ] verify
[ ] query_history_record
[ ] favorite_list
[ ] remove_favorite
[ ] get_nav_line
[ ] get_setting
[ ] set_setting (preference)
[ ] get_parking_space_list
[ ] get_my_parking_space_list
[ ] add_parking_space
[ ] rating


## data design
* user
  * name
  * email
  * mobile
  * password
* parking-space
  * status
  * user-id
  * geo-point
  * available-time
* favorite
  * parking-space-id


## architecture

END
