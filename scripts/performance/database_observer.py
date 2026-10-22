#!/usr/bin/env python3
"""Read existing pg_stat_statements; no resets, DDL or query-text export."""
import argparse, json, pathlib, time
from develop import sql
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('folder',type=pathlib.Path);args=parser.parse_args()
folder=args.folder
if not (folder/'progress.json').is_file():parser.error('Falta progress.json en la serie')
output=folder/'sql-server.ndjson'
query="""SELECT coalesce(json_agg(row_to_json(s)),'[]'::json) FROM (
 SELECT queryid::text AS id,calls,total_exec_time,total_plan_time,rows,shared_blks_hit,shared_blks_read,
 temp_blks_written,shared_blk_read_time,shared_blk_write_time,
 CASE WHEN query ILIKE '%horarios_estilistas%' THEN 'horarios'
      WHEN query ILIKE '%bloqueos_horarios%' THEN 'bloqueos'
      WHEN query ILIKE '%from citas%' THEN 'citas'
      WHEN query ILIKE '%from servicios%' THEN 'servicios'
      WHEN query ILIKE '%from usuarios%' THEN 'usuarios'
      WHEN query ILIKE '%from estilistas%' THEN 'estilistas'
      WHEN query ILIKE '%pg_stat%' THEN 'diagnostico'
      ELSE 'otros' END AS category
 FROM extensions.pg_stat_statements WHERE dbid=(SELECT oid FROM pg_database WHERE datname=current_database())
 ) s"""
with output.open('a') as out:
    while True:
        begin=time.time()
        try:row={'time':begin,'statements':json.loads(sql(query))}
        except Exception as error:row={'time':begin,'errorType':type(error).__name__}
        out.write(json.dumps(row)+'\n');out.flush()
        progress=json.loads((folder/'progress.json').read_text())
        if progress.get('state')=='complete':break
        time.sleep(30)
