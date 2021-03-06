

extern crate "rustc-serialize" as serialize;
extern crate rumblebars;

use std::fs::File;
use std::io::Read;

fn compile() {
  let mut f = File::open("../data/template.hbs").unwrap();
  let mut s = String::new();
  f.read_to_string(&mut s);

  for _ in (0..2000) {
    s.parse::<rumblebars::Template>();
  }
}

struct NullWrite;

impl ::std::io::Write for NullWrite {
  fn write(&mut self, buf: &[u8]) -> ::std::io::Result<usize> { Ok(buf.len()) }
  fn flush(&mut self) -> ::std::io::Result<()> { Ok(()) }
}

fn expand() {
  let t = {
    let mut f = File::open("../data/template.hbs").unwrap();
    let mut s = String::new();
    f.read_to_string(&mut s);
    s.parse().unwrap_or_else(|e| {
      println!("cannot parse template");
      panic!(format!("{:?}", e))
    })
  };

  let j = {
    let mut f = File::open("../data/template.json").unwrap();
    let mut s = String::new();
    f.read_to_string(&mut s);
    s.parse::<::serialize::json::Json>().unwrap_or_else(|e| {
      println!("cannot parse json data");
      panic!(format!("{:?}", e))
    })
  };

  for _ in (0..2000) {
    let mut vec = Vec::<u8>::new();
    {
      let mut n = NullWrite;
      let mut out = ::std::io::BufWriter::new(&mut n as &mut ::std::io::Write);
      // let mut out = ::std::io::BufWriter::new(&mut vec as &mut ::std::io::Write);
      ::rumblebars::eval(&t, &j, &mut out, &::std::default::Default::default());
    }
  }
}

fn main() {
  compile();
  expand();
}